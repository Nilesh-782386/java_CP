import { useState } from "react";
import { FileText, AlertCircle, CheckCircle2, TrendingUp, TrendingDown } from "lucide-react";
import { useMutation } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { DisclaimerBanner } from "@/components/DisclaimerBanner";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import type { ReportAnalysis } from "@shared/schema";
import { apiRequest } from "@/lib/queryClient";
import { cn } from "@/lib/utils";

const bloodParameters = [
  { key: "hemoglobin", label: "Hemoglobin", unit: "g/dL", normalRange: "12-16" },
  { key: "wbc", label: "WBC Count", unit: "cells/µL", normalRange: "4000-11000" },
  { key: "platelets", label: "Platelet Count", unit: "cells/µL", normalRange: "150000-450000" },
  { key: "rbc", label: "RBC Count", unit: "million cells/µL", normalRange: "4.5-5.5" },
  { key: "bloodSugar", label: "Blood Sugar (Fasting)", unit: "mg/dL", normalRange: "70-100", optional: true },
  { key: "cholesterol", label: "Total Cholesterol", unit: "mg/dL", normalRange: "<200", optional: true },
];

export default function ReportAnalyzer() {
  const [formData, setFormData] = useState<Record<string, string>>({});
  const { toast } = useToast();

  const analyzeMutation = useMutation({
    mutationFn: async (data: any) => {
      return apiRequest<ReportAnalysis>("POST", "/api/analyze-report", data);
    },
    onError: (error: Error) => {
      toast({
        title: "Error",
        description: error.message || "Failed to analyze report",
        variant: "destructive",
      });
    },
  });

  const handleInputChange = (key: string, value: string) => {
    setFormData((prev) => ({ ...prev, [key]: value }));
  };

  const handleAnalyze = () => {
    const data: any = {};
    let hasRequired = true;

    bloodParameters.forEach((param) => {
      const value = formData[param.key];
      if (value && value.trim() !== "") {
        data[param.key] = parseFloat(value);
      } else if (!param.optional) {
        hasRequired = false;
      }
    });

    if (!hasRequired) {
      toast({
        title: "Missing Required Fields",
        description: "Please fill in all required blood test parameters",
        variant: "destructive",
      });
      return;
    }

    analyzeMutation.mutate(data);
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "normal":
        return <Badge className="bg-green-500">Normal</Badge>;
      case "low":
        return <Badge variant="destructive">Low</Badge>;
      case "high":
        return <Badge variant="destructive">High</Badge>;
      default:
        return <Badge variant="secondary">{status}</Badge>;
    }
  };

  const getOverallStatusColor = (status: string) => {
    switch (status) {
      case "healthy":
        return "text-green-600";
      case "needs-attention":
        return "text-yellow-600";
      case "concerning":
        return "text-orange-600";
      case "critical":
        return "text-red-600";
      default:
        return "text-gray-600";
    }
  };

  return (
    <div className="min-h-screen py-8 md:py-12">
      <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-4" data-testid="page-title">Medical Report Analyzer</h1>
          <p className="text-lg text-muted-foreground leading-relaxed max-w-3xl">
            Input your blood test values to receive educational feedback comparing them to standard reference ranges
          </p>
        </div>

        <div className="mb-8">
          <DisclaimerBanner />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
          {/* Input Form */}
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <FileText className="h-6 w-6 text-primary" />
                  Blood Test Parameters
                </CardTitle>
                <CardDescription>Enter your blood test values below</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {bloodParameters.map((param) => (
                  <div key={param.key} className="space-y-2">
                    <Label htmlFor={param.key} className="flex items-center justify-between">
                      <span>
                        {param.label}
                        {param.optional && <span className="text-muted-foreground ml-1">(optional)</span>}
                      </span>
                      <span className="text-xs text-muted-foreground">Normal: {param.normalRange}</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id={param.key}
                        type="number"
                        step="0.01"
                        placeholder={`Enter ${param.label.toLowerCase()}`}
                        value={formData[param.key] || ""}
                        onChange={(e) => handleInputChange(param.key, e.target.value)}
                        className="pr-16"
                        data-testid={`input-${param.key}`}
                      />
                      <span className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground">
                        {param.unit}
                      </span>
                    </div>
                  </div>
                ))}

                <Button
                  onClick={handleAnalyze}
                  disabled={analyzeMutation.isPending}
                  className="w-full"
                  size="lg"
                  data-testid="button-analyze"
                >
                  {analyzeMutation.isPending ? "Analyzing..." : "Analyze Report"}
                </Button>
              </CardContent>
            </Card>
          </div>

          {/* Results */}
          <div className="lg:col-span-3">
            {!analyzeMutation.data && !analyzeMutation.isPending ? (
              <Card className="h-full flex items-center justify-center min-h-[400px]">
                <CardContent className="text-center py-12">
                  <FileText className="h-16 w-16 mx-auto mb-4 text-muted-foreground opacity-50" />
                  <h3 className="font-semibold text-lg mb-2">No Analysis Yet</h3>
                  <p className="text-sm text-muted-foreground max-w-sm mx-auto">
                    Enter your blood test values and click "Analyze Report" to see educational insights
                  </p>
                </CardContent>
              </Card>
            ) : analyzeMutation.isPending ? (
              <Card>
                <CardContent className="py-12">
                  <div className="flex flex-col items-center justify-center space-y-4">
                    <div className="h-12 w-12 rounded-full border-4 border-primary border-t-transparent animate-spin" />
                    <p className="text-sm text-muted-foreground">Analyzing your report...</p>
                  </div>
                </CardContent>
              </Card>
            ) : analyzeMutation.data ? (
              <div className="space-y-6">
                {/* Overall Status */}
                <Card className="border-primary/50">
                  <CardHeader>
                    <div className="flex items-center justify-between flex-wrap gap-4">
                      <div>
                        <CardTitle className="text-2xl mb-2">Analysis Results</CardTitle>
                        <Badge variant="secondary" className={cn("text-base", getOverallStatusColor(analyzeMutation.data.overallStatus))}>
                          {analyzeMutation.data.overallStatus.replace("-", " ").toUpperCase()}
                        </Badge>
                      </div>
                      {analyzeMutation.data.flaggedParameters.length > 0 && (
                        <div className="text-right">
                          <p className="text-sm text-muted-foreground mb-1">Flagged Parameters</p>
                          <p className="text-3xl font-bold text-destructive">{analyzeMutation.data.flaggedParameters.length}</p>
                        </div>
                      )}
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground leading-relaxed" data-testid="text-summary">
                      {analyzeMutation.data.summary}
                    </p>
                  </CardContent>
                </Card>

                {/* Parameters Table */}
                <Card>
                  <CardHeader>
                    <CardTitle>Parameter Details</CardTitle>
                    <CardDescription>Comparison of your values against standard reference ranges</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {analyzeMutation.data.parameters.map((param, index) => (
                        <div
                          key={index}
                          className={cn(
                            "p-4 rounded-lg border-2 transition-colors",
                            param.status === "normal" ? "bg-green-50 dark:bg-green-950/20 border-green-200 dark:border-green-900" :
                            param.status === "low" ? "bg-red-50 dark:bg-red-950/20 border-red-200 dark:border-red-900" :
                            param.status === "high" ? "bg-red-50 dark:bg-red-950/20 border-red-200 dark:border-red-900" :
                            "bg-muted/50 border-border"
                          )}
                          data-testid={`parameter-${index}`}
                        >
                          <div className="flex items-center justify-between gap-4 flex-wrap">
                            <div className="flex-1 min-w-[200px]">
                              <div className="flex items-center gap-2 mb-1">
                                <h4 className="font-semibold">{param.name}</h4>
                                {getStatusBadge(param.status)}
                              </div>
                              <p className="text-sm text-muted-foreground">
                                Normal range: {param.normalMin} - {param.normalMax} {param.unit}
                              </p>
                            </div>
                            <div className="text-right">
                              <div className="flex items-center justify-end gap-2 mb-1">
                                {param.status === "high" && <TrendingUp className="h-5 w-5 text-destructive" />}
                                {param.status === "low" && <TrendingDown className="h-5 w-5 text-destructive" />}
                                {param.status === "normal" && <CheckCircle2 className="h-5 w-5 text-green-600" />}
                                <span className="text-2xl font-mono font-bold">
                                  {param.value}
                                </span>
                              </div>
                              <p className="text-sm text-muted-foreground">{param.unit}</p>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>

                {/* Recommendations */}
                {analyzeMutation.data.recommendations.length > 0 && (
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        <AlertCircle className="h-5 w-5 text-primary" />
                        Recommendations
                      </CardTitle>
                      <CardDescription>Educational guidance based on your results</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <ul className="space-y-3">
                        {analyzeMutation.data.recommendations.map((recommendation, index) => (
                          <li key={index} className="flex items-start gap-3" data-testid={`recommendation-${index}`}>
                            <div className="flex items-center justify-center w-6 h-6 rounded-full bg-primary/10 text-primary font-semibold text-sm flex-shrink-0 mt-0.5">
                              {index + 1}
                            </div>
                            <p className="text-sm leading-relaxed flex-1">{recommendation}</p>
                          </li>
                        ))}
                      </ul>
                    </CardContent>
                  </Card>
                )}
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  );
}
