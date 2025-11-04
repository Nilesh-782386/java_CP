import type { Express } from "express";
import { createServer, type Server } from "http";
import { symptomsDatabase } from "./data/symptoms";
import { diseasesDatabase } from "./data/diseases";
import { chatbotKnowledgeBase } from "./data/chatbot-kb";
import { medicalTestsDatabase } from "./data/tests";
import { treatmentCostsDatabase, treatmentsList } from "./data/treatments";
import { bloodParameterRanges, analyzeParameter, generateRecommendations } from "./data/blood-parameters";
import {
  checkSymptomsRequestSchema,
  chatRequestSchema,
  costEstimationRequestSchema,
  reportAnalysisRequestSchema,
  type SymptomCheckResult,
  type ChatResponse,
  type TestRecommendation,
  type CostEstimation,
  type ReportAnalysis,
  type BloodParameter,
} from "@shared/schema";

export async function registerRoutes(app: Express): Promise<Server> {
  // Get all symptoms
  app.get("/api/symptoms", (_req, res) => {
    res.json(symptomsDatabase);
  });

  // Get all diseases (for test lookup)
  app.get("/api/diseases", (_req, res) => {
    res.json(diseasesDatabase);
  });

  // Get all treatment types (for cost estimator)
  app.get("/api/treatments", (_req, res) => {
    res.json(treatmentsList);
  });

  // Analyze symptoms and return possible diseases
  app.post("/api/check-symptoms", (req, res) => {
    try {
      const validation = checkSymptomsRequestSchema.safeParse(req.body);
      if (!validation.success) {
        return res.status(400).json({ message: validation.error.message });
      }

      const { symptomIds } = validation.data;
      const results: SymptomCheckResult[] = [];

      // Check each disease for matches
      diseasesDatabase.forEach((disease) => {
        const matchedSymptoms = disease.commonSymptoms.filter((symptomId) =>
          symptomIds.includes(symptomId)
        );

        if (matchedSymptoms.length > 0) {
          const matchPercentage = Math.round(
            (matchedSymptoms.length / disease.commonSymptoms.length) * 100
          );

          // Get symptom names
          const matchedSymptomNames = matchedSymptoms.map(
            (id) => symptomsDatabase.find((s) => s.id === id)?.name || id
          );

          results.push({
            disease,
            matchPercentage,
            matchedSymptoms: matchedSymptomNames,
          });
        }
      });

      // Sort by match percentage (highest first)
      results.sort((a, b) => b.matchPercentage - a.matchPercentage);

      // Return top 5 results
      res.json(results.slice(0, 5));
    } catch (error) {
      res.status(500).json({ message: "Failed to analyze symptoms" });
    }
  });

  // Chat endpoint - rule-based responses
  app.post("/api/chat", (req, res) => {
    try {
      const validation = chatRequestSchema.safeParse(req.body);
      if (!validation.success) {
        return res.status(400).json({ message: validation.error.message });
      }

      const { message } = validation.data;
      const messageLower = message.toLowerCase();

      // Find matching knowledge base entry
      let bestMatch = chatbotKnowledgeBase.find((kb) => kb.category === "fallback");
      let highestScore = 0;

      chatbotKnowledgeBase.forEach((kb) => {
        if (kb.category === "fallback") return;

        let score = 0;
        kb.keywords.forEach((keyword) => {
          if (messageLower.includes(keyword.toLowerCase())) {
            score += keyword.length; // Longer keywords get more weight
          }
        });

        if (score > highestScore) {
          highestScore = score;
          bestMatch = kb;
        }
      });

      const response: ChatResponse = {
        response: bestMatch?.response || "I'm not sure about that. Could you rephrase your question?",
        relatedTopics: [],
      };

      res.json(response);
    } catch (error) {
      res.status(500).json({ message: "Failed to process chat message" });
    }
  });

  // Get test recommendations for a disease
  app.get("/api/tests/:diseaseId", (req, res) => {
    try {
      const { diseaseId } = req.params;
      const disease = diseasesDatabase.find((d) => d.id === diseaseId);

      if (!disease) {
        return res.status(404).json({ message: "Disease not found" });
      }

      const testData = medicalTestsDatabase[diseaseId];
      if (!testData) {
        return res.status(404).json({ message: "Test recommendations not available for this disease" });
      }

      const recommendation: TestRecommendation = {
        diseaseId: disease.id,
        diseaseName: disease.name,
        recommendedTests: testData.recommended,
        testsToAvoid: testData.toAvoid,
        reasoning: testData.reasoning,
      };

      res.json(recommendation);
    } catch (error) {
      res.status(500).json({ message: "Failed to get test recommendations" });
    }
  });

  // Estimate treatment cost
  app.post("/api/estimate-cost", (req, res) => {
    try {
      const validation = costEstimationRequestSchema.safeParse(req.body);
      if (!validation.success) {
        return res.status(400).json({ message: validation.error.message });
      }

      const { treatmentType, hospitalType } = validation.data;

      // Find treatment in database (case-insensitive partial match)
      const treatmentKey = Object.keys(treatmentCostsDatabase).find((key) =>
        treatmentCostsDatabase[key].name.toLowerCase() === treatmentType.toLowerCase()
      );

      if (!treatmentKey) {
        return res.status(404).json({ message: "Treatment not found in database" });
      }

      const treatment = treatmentCostsDatabase[treatmentKey];
      let costData;

      switch (hospitalType) {
        case "government":
          costData = treatment.government;
          break;
        case "semi-private":
          costData = treatment.semiPrivate;
          break;
        case "private":
          costData = treatment.private;
          break;
        default:
          return res.status(400).json({ message: "Invalid hospital type" });
      }

      const estimation: CostEstimation = {
        treatmentName: treatment.name,
        hospitalType: hospitalType === "semi-private" ? "Semi-Private Hospital" : 
                      hospitalType === "government" ? "Government Hospital" : "Private Hospital",
        minCost: costData.min,
        maxCost: costData.max,
        averageCost: costData.avg,
        factors: treatment.factors,
        disclaimer: "These are estimated costs based on average data from various sources across India. Actual costs may vary significantly based on your location, specific hospital, doctor's fees, complications, and individual medical needs. Always confirm costs directly with the healthcare facility before proceeding with treatment.",
      };

      res.json(estimation);
    } catch (error) {
      res.status(500).json({ message: "Failed to estimate cost" });
    }
  });

  // Analyze blood report
  app.post("/api/analyze-report", (req, res) => {
    try {
      const validation = reportAnalysisRequestSchema.safeParse(req.body);
      if (!validation.success) {
        return res.status(400).json({ message: validation.error.message });
      }

      const reportData = validation.data;
      const parameters: BloodParameter[] = [];
      const flaggedParameters: string[] = [];
      const paramAnalysis: Array<{ key: string; value: number; status: string; severity: string }> = [];

      // Analyze each parameter
      Object.entries(reportData).forEach(([key, value]) => {
        if (value !== undefined && bloodParameterRanges[key]) {
          const range = bloodParameterRanges[key];
          const analysis = analyzeParameter(key, value);

          parameters.push({
            name: range.name,
            value,
            unit: range.unit,
            normalMin: range.normalMin,
            normalMax: range.normalMax,
            status: analysis.status,
            severity: analysis.severity,
          });

          if (analysis.status !== "normal") {
            flaggedParameters.push(range.name);
          }

          paramAnalysis.push({
            key,
            value,
            status: analysis.status,
            severity: analysis.severity,
          });
        }
      });

      // Determine overall status
      const hasCritical = paramAnalysis.some((p) => p.severity === "critical");
      const hasConcerning = paramAnalysis.some((p) => p.severity === "concerning");
      const hasBorderline = paramAnalysis.some((p) => p.severity === "borderline");

      let overallStatus: "healthy" | "needs-attention" | "concerning" | "critical" = "healthy";
      if (hasCritical) {
        overallStatus = "critical";
      } else if (hasConcerning) {
        overallStatus = "concerning";
      } else if (hasBorderline) {
        overallStatus = "needs-attention";
      }

      // Generate summary
      let summary = "";
      if (flaggedParameters.length === 0) {
        summary = "All analyzed blood parameters are within normal reference ranges. This is a positive indicator of your overall health.";
      } else if (hasCritical) {
        summary = `Critical attention required: ${flaggedParameters.length} parameter(s) are outside normal ranges, with some in critical levels. Immediate medical consultation is strongly recommended.`;
      } else if (hasConcerning) {
        summary = `${flaggedParameters.length} parameter(s) are outside normal ranges and require medical attention. Please schedule an appointment with your healthcare provider soon.`;
      } else {
        summary = `${flaggedParameters.length} parameter(s) are slightly outside normal ranges. While not immediately concerning, these should be monitored and discussed with your doctor.`;
      }

      const recommendations = generateRecommendations(paramAnalysis);

      const analysis: ReportAnalysis = {
        parameters,
        overallStatus,
        summary,
        recommendations,
        flaggedParameters,
      };

      res.json(analysis);
    } catch (error) {
      console.error("Report analysis error:", error);
      res.status(500).json({ message: "Failed to analyze report" });
    }
  });

  const httpServer = createServer(app);
  return httpServer;
}
