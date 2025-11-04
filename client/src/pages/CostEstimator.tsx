import { useState } from "react";
import { DollarSign, TrendingUp, Info, Building2, Building, Home } from "lucide-react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { DisclaimerBanner } from "@/components/DisclaimerBanner";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import type { CostEstimation } from "@shared/schema";
import { apiRequest } from "@/lib/queryClient";

const hospitalTypes = [
  { value: "government", label: "Government Hospital", Icon: Home },
  { value: "semi-private", label: "Semi-Private Hospital", Icon: Building },
  { value: "private", label: "Private Hospital", Icon: Building2 },
];

export default function CostEstimator() {
  const [treatmentType, setTreatmentType] = useState("");
  const [hospitalType, setHospitalType] = useState<"government" | "private" | "semi-private">("government");
  const { toast } = useToast();

  const { data: treatments, isLoading: treatmentsLoading } = useQuery<string[]>({
    queryKey: ["/api/treatments"],
  });

  const estimateMutation = useMutation({
    mutationFn: async (data: { treatmentType: string; hospitalType: "government" | "private" | "semi-private" }) => {
      return apiRequest<CostEstimation>("POST", "/api/estimate-cost", data);
    },
    onError: (error: Error) => {
      toast({
        title: "Error",
        description: error.message || "Failed to estimate cost",
        variant: "destructive",
      });
    },
  });

  const handleEstimate = () => {
    if (!treatmentType) {
      toast({
        title: "Missing Information",
        description: "Please select a treatment type",
        variant: "destructive",
      });
      return;
    }
    estimateMutation.mutate({ treatmentType, hospitalType });
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 0,
    }).format(amount);
  };

  return (
    <div className="min-h-screen py-8 md:py-12">
      <div className="mx-auto max-w-5xl px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-4" data-testid="page-title">Treatment Cost Estimator</h1>
          <p className="text-lg text-muted-foreground leading-relaxed max-w-3xl">
            Get estimated costs for medical treatments and procedures at different types of hospitals in India
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
                  <Building2 className="h-6 w-6 text-primary" />
                  Estimate Parameters
                </CardTitle>
                <CardDescription>Select treatment and hospital type to get cost estimates</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                {/* Treatment Selection */}
                <div className="space-y-2">
                  <Label htmlFor="treatment">Treatment or Procedure</Label>
                  {treatmentsLoading ? (
                    <Skeleton className="h-10 w-full" />
                  ) : (
                    <Select value={treatmentType} onValueChange={setTreatmentType}>
                      <SelectTrigger id="treatment" data-testid="select-treatment">
                        <SelectValue placeholder="Select a treatment" />
                      </SelectTrigger>
                      <SelectContent>
                        {treatments?.map((treatment) => (
                          <SelectItem key={treatment} value={treatment}>
                            {treatment}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                </div>

                {/* Hospital Type */}
                <div className="space-y-2">
                  <Label htmlFor="hospital-type">Hospital Type</Label>
                  <Select value={hospitalType} onValueChange={(value: any) => setHospitalType(value)}>
                    <SelectTrigger id="hospital-type" data-testid="select-hospital-type">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {hospitalTypes.map((type) => {
                        const Icon = type.Icon;
                        return (
                          <SelectItem key={type.value} value={type.value}>
                            <span className="flex items-center gap-2">
                              <Icon className="h-4 w-4" />
                              <span>{type.label}</span>
                            </span>
                          </SelectItem>
                        );
                      })}
                    </SelectContent>
                  </Select>
                </div>

                <Button
                  onClick={handleEstimate}
                  disabled={!treatmentType || estimateMutation.isPending}
                  className="w-full"
                  size="lg"
                  data-testid="button-estimate"
                >
                  {estimateMutation.isPending ? "Calculating..." : "Estimate Cost"}
                </Button>

                <div className="pt-4 border-t">
                  <div className="flex items-start gap-2">
                    <Info className="h-5 w-5 text-muted-foreground mt-0.5 flex-shrink-0" />
                    <p className="text-sm text-muted-foreground leading-relaxed">
                      Cost estimates are based on average data and may vary significantly based on location, hospital reputation, doctor experience, and individual patient needs.
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Results */}
          <div className="lg:col-span-3">
            {!estimateMutation.data && !estimateMutation.isPending ? (
              <Card className="h-full flex items-center justify-center min-h-[400px]">
                <CardContent className="text-center py-12">
                  <DollarSign className="h-16 w-16 mx-auto mb-4 text-muted-foreground opacity-50" />
                  <h3 className="font-semibold text-lg mb-2">No Estimate Yet</h3>
                  <p className="text-sm text-muted-foreground max-w-sm mx-auto">
                    Select a treatment and hospital type, then click "Estimate Cost" to see pricing information
                  </p>
                </CardContent>
              </Card>
            ) : estimateMutation.isPending ? (
              <div className="space-y-6">
                <Skeleton className="h-48 w-full" />
                <Skeleton className="h-64 w-full" />
              </div>
            ) : estimateMutation.data ? (
              <div className="space-y-6">
                {/* Cost Overview */}
                <Card className="border-primary/50">
                  <CardHeader>
                    <div className="flex items-center justify-between flex-wrap gap-4">
                      <div>
                        <CardTitle className="text-2xl mb-2">{estimateMutation.data.treatmentName}</CardTitle>
                        <Badge variant="secondary" className="text-sm">
                          {estimateMutation.data.hospitalType}
                        </Badge>
                      </div>
                      <div className="text-right">
                        <p className="text-sm text-muted-foreground mb-1">Average Cost</p>
                        <p className="text-3xl md:text-4xl font-bold text-primary font-mono" data-testid="text-average-cost">
                          {formatCurrency(estimateMutation.data.averageCost)}
                        </p>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="text-center p-4 bg-muted/50 rounded-lg">
                        <p className="text-sm text-muted-foreground mb-1">Minimum Cost</p>
                        <p className="text-xl font-semibold font-mono" data-testid="text-min-cost">
                          {formatCurrency(estimateMutation.data.minCost)}
                        </p>
                      </div>
                      <div className="text-center p-4 bg-muted/50 rounded-lg">
                        <p className="text-sm text-muted-foreground mb-1">Maximum Cost</p>
                        <p className="text-xl font-semibold font-mono" data-testid="text-max-cost">
                          {formatCurrency(estimateMutation.data.maxCost)}
                        </p>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* Cost Factors */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <TrendingUp className="h-5 w-5 text-primary" />
                      Factors Affecting Cost
                    </CardTitle>
                    <CardDescription>Various elements that influence the final treatment cost</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      {estimateMutation.data.factors.map((factor, index) => (
                        <div key={index} className="flex items-start gap-3 p-4 bg-muted/30 rounded-lg" data-testid={`factor-${index}`}>
                          <div className="flex items-center justify-center w-8 h-8 rounded-full bg-primary/10 text-primary font-semibold text-sm flex-shrink-0">
                            {index + 1}
                          </div>
                          <div className="flex-1">
                            <p className="font-medium mb-1">{factor.name}</p>
                            <p className="text-sm text-muted-foreground leading-relaxed">{factor.impact}</p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>

                {/* Disclaimer */}
                <Card className="border-destructive/50 bg-destructive/5">
                  <CardContent className="pt-6">
                    <div className="flex items-start gap-3">
                      <Info className="h-5 w-5 text-destructive flex-shrink-0 mt-0.5" />
                      <div>
                        <p className="font-semibold mb-2 text-sm">Important Note</p>
                        <p className="text-sm text-muted-foreground leading-relaxed">
                          {estimateMutation.data.disclaimer}
                        </p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  );
}
