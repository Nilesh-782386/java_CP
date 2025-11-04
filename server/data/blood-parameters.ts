export interface BloodParameterRange {
  name: string;
  unit: string;
  normalMin: number;
  normalMax: number;
  criticalLow?: number;
  criticalHigh?: number;
}

export const bloodParameterRanges: Record<string, BloodParameterRange> = {
  hemoglobin: {
    name: "Hemoglobin",
    unit: "g/dL",
    normalMin: 12,
    normalMax: 16,
    criticalLow: 7,
    criticalHigh: 20,
  },
  wbc: {
    name: "WBC Count",
    unit: "cells/µL",
    normalMin: 4000,
    normalMax: 11000,
    criticalLow: 2000,
    criticalHigh: 30000,
  },
  platelets: {
    name: "Platelet Count",
    unit: "cells/µL",
    normalMin: 150000,
    normalMax: 450000,
    criticalLow: 50000,
    criticalHigh: 1000000,
  },
  rbc: {
    name: "RBC Count",
    unit: "million cells/µL",
    normalMin: 4.5,
    normalMax: 5.5,
    criticalLow: 3.0,
    criticalHigh: 7.0,
  },
  bloodSugar: {
    name: "Blood Sugar (Fasting)",
    unit: "mg/dL",
    normalMin: 70,
    normalMax: 100,
    criticalLow: 50,
    criticalHigh: 300,
  },
  cholesterol: {
    name: "Total Cholesterol",
    unit: "mg/dL",
    normalMin: 0,
    normalMax: 200,
    criticalLow: 0,
    criticalHigh: 400,
  },
};

export function analyzeParameter(
  paramKey: string,
  value: number
): {
  status: "normal" | "low" | "high";
  severity: "normal" | "borderline" | "concerning" | "critical";
} {
  const range = bloodParameterRanges[paramKey];
  if (!range) {
    return { status: "normal", severity: "normal" };
  }

  let status: "normal" | "low" | "high" = "normal";
  let severity: "normal" | "borderline" | "concerning" | "critical" = "normal";

  if (value < range.normalMin) {
    status = "low";
    if (range.criticalLow && value < range.criticalLow) {
      severity = "critical";
    } else if (value < range.normalMin * 0.9) {
      severity = "concerning";
    } else {
      severity = "borderline";
    }
  } else if (value > range.normalMax) {
    status = "high";
    if (range.criticalHigh && value > range.criticalHigh) {
      severity = "critical";
    } else if (value > range.normalMax * 1.2) {
      severity = "concerning";
    } else {
      severity = "borderline";
    }
  }

  return { status, severity };
}

export function generateRecommendations(
  parameters: Array<{ key: string; value: number; status: string; severity: string }>
): string[] {
  const recommendations: string[] = [];
  const criticalParams = parameters.filter((p) => p.severity === "critical");
  const concerningParams = parameters.filter((p) => p.severity === "concerning");
  const borderlineParams = parameters.filter((p) => p.severity === "borderline");

  if (criticalParams.length > 0) {
    recommendations.push(
      "⚠️ URGENT: Some parameters are in critical range. Seek immediate medical attention and consult your doctor as soon as possible."
    );
  }

  if (concerningParams.length > 0) {
    recommendations.push(
      "Schedule an appointment with your healthcare provider within the next few days to discuss concerning values."
    );
  }

  // Specific recommendations based on parameters
  parameters.forEach((param) => {
    if (param.key === "hemoglobin" && param.status === "low") {
      recommendations.push(
        "Low hemoglobin may indicate anemia. Increase iron-rich foods (spinach, red meat, beans) and consider iron supplements after consulting your doctor."
      );
    }

    if (param.key === "wbc" && param.status === "high") {
      recommendations.push(
        "Elevated white blood cell count may indicate infection or inflammation. Your doctor may recommend further tests to identify the cause."
      );
    }

    if (param.key === "wbc" && param.status === "low") {
      recommendations.push(
        "Low white blood cell count may affect your immune system. Avoid crowds and practice good hygiene. Consult your doctor about potential causes."
      );
    }

    if (param.key === "platelets" && param.status === "low") {
      recommendations.push(
        "Low platelet count increases bleeding risk. Avoid contact sports and activities that may cause injury. Inform your doctor before any dental or surgical procedures."
      );
    }

    if (param.key === "bloodSugar" && param.status === "high") {
      recommendations.push(
        "Elevated blood sugar requires attention. Limit refined carbohydrates and sugary foods, increase physical activity, and consult your doctor about diabetes screening."
      );
    }

    if (param.key === "bloodSugar" && param.status === "low") {
      recommendations.push(
        "Low blood sugar can cause dizziness and weakness. Keep quick-acting carbohydrates handy and discuss with your doctor if this is recurring."
      );
    }

    if (param.key === "cholesterol" && param.status === "high") {
      recommendations.push(
        "High cholesterol increases cardiovascular risk. Adopt a heart-healthy diet (reduce saturated fats, increase fiber), exercise regularly, and discuss statin therapy with your doctor."
      );
    }
  });

  if (borderlineParams.length > 0 && criticalParams.length === 0 && concerningParams.length === 0) {
    recommendations.push(
      "Some values are borderline. Maintain a healthy lifestyle with balanced diet, regular exercise, and stress management."
    );
  }

  if (recommendations.length === 0) {
    recommendations.push(
      "Your blood test parameters appear to be within normal ranges. Continue maintaining a healthy lifestyle."
    );
    recommendations.push(
      "Regular health check-ups are important for early detection of any changes."
    );
  }

  recommendations.push(
    "Remember: These are educational insights. Always discuss your results with a qualified healthcare provider for personalized medical advice."
  );

  return recommendations;
}
