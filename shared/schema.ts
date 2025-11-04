import { z } from "zod";

// Symptom Checker Schemas
export const symptomSchema = z.object({
  id: z.string(),
  name: z.string(),
  category: z.string(),
  description: z.string().optional(),
});

export const diseaseSchema = z.object({
  id: z.string(),
  name: z.string(),
  description: z.string(),
  severity: z.enum(["low", "moderate", "high"]),
  commonSymptoms: z.array(z.string()),
  treatments: z.array(z.string()),
  whenToSeekHelp: z.string(),
});

export const symptomCheckResultSchema = z.object({
  disease: diseaseSchema,
  matchPercentage: z.number(),
  matchedSymptoms: z.array(z.string()),
});

export const checkSymptomsRequestSchema = z.object({
  symptomIds: z.array(z.string()).min(1, "Please select at least one symptom"),
});

// Chatbot Schemas
export const chatMessageSchema = z.object({
  id: z.string(),
  role: z.enum(["user", "assistant"]),
  content: z.string(),
  timestamp: z.number(),
});

export const chatRequestSchema = z.object({
  message: z.string().min(1, "Message cannot be empty"),
});

export const chatResponseSchema = z.object({
  response: z.string(),
  relatedTopics: z.array(z.string()).optional(),
});

// Test Information Schemas
export const medicalTestSchema = z.object({
  id: z.string(),
  name: z.string(),
  description: z.string(),
  purpose: z.string(),
  costRange: z.string(),
  preparation: z.string().optional(),
});

export const testRecommendationSchema = z.object({
  diseaseId: z.string(),
  diseaseName: z.string(),
  recommendedTests: z.array(medicalTestSchema),
  testsToAvoid: z.array(medicalTestSchema),
  reasoning: z.string(),
});

// Cost Estimation Schemas
export const costEstimationRequestSchema = z.object({
  treatmentType: z.string().min(1, "Please select a treatment type"),
  hospitalType: z.enum(["government", "private", "semi-private"]),
  city: z.string().optional(),
});

export const costEstimationSchema = z.object({
  treatmentName: z.string(),
  hospitalType: z.string(),
  minCost: z.number(),
  maxCost: z.number(),
  averageCost: z.number(),
  factors: z.array(z.object({
    name: z.string(),
    impact: z.string(),
  })),
  disclaimer: z.string(),
});

// Report Analyzer Schemas
export const bloodParameterSchema = z.object({
  name: z.string(),
  value: z.number(),
  unit: z.string(),
  normalMin: z.number(),
  normalMax: z.number(),
  status: z.enum(["normal", "low", "high"]),
  severity: z.enum(["normal", "borderline", "concerning", "critical"]).optional(),
});

export const reportAnalysisRequestSchema = z.object({
  hemoglobin: z.number().min(0).max(30),
  wbc: z.number().min(0).max(50000),
  platelets: z.number().min(0).max(1000000),
  rbc: z.number().min(0).max(10),
  bloodSugar: z.number().min(0).max(600).optional(),
  cholesterol: z.number().min(0).max(500).optional(),
});

export const reportAnalysisSchema = z.object({
  parameters: z.array(bloodParameterSchema),
  overallStatus: z.enum(["healthy", "needs-attention", "concerning", "critical"]),
  summary: z.string(),
  recommendations: z.array(z.string()),
  flaggedParameters: z.array(z.string()),
});

// Type exports
export type Symptom = z.infer<typeof symptomSchema>;
export type Disease = z.infer<typeof diseaseSchema>;
export type SymptomCheckResult = z.infer<typeof symptomCheckResultSchema>;
export type CheckSymptomsRequest = z.infer<typeof checkSymptomsRequestSchema>;

export type ChatMessage = z.infer<typeof chatMessageSchema>;
export type ChatRequest = z.infer<typeof chatRequestSchema>;
export type ChatResponse = z.infer<typeof chatResponseSchema>;

export type MedicalTest = z.infer<typeof medicalTestSchema>;
export type TestRecommendation = z.infer<typeof testRecommendationSchema>;

export type CostEstimationRequest = z.infer<typeof costEstimationRequestSchema>;
export type CostEstimation = z.infer<typeof costEstimationSchema>;

export type BloodParameter = z.infer<typeof bloodParameterSchema>;
export type ReportAnalysisRequest = z.infer<typeof reportAnalysisRequestSchema>;
export type ReportAnalysis = z.infer<typeof reportAnalysisSchema>;
