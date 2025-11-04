import { useState } from "react";
import { Search, X, AlertCircle, CheckCircle2, Info } from "lucide-react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { Progress } from "@/components/ui/progress";
import { DisclaimerBanner } from "@/components/DisclaimerBanner";
import { Skeleton } from "@/components/ui/skeleton";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { useToast } from "@/hooks/use-toast";
import type { Symptom, SymptomCheckResult } from "@shared/schema";
import { apiRequest } from "@/lib/queryClient";

export default function SymptomChecker() {
  const [selectedSymptoms, setSelectedSymptoms] = useState<string[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const { toast } = useToast();

  const { data: symptoms, isLoading: symptomsLoading } = useQuery<Symptom[]>({
    queryKey: ["/api/symptoms"],
  });

  const checkSymptomsMutation = useMutation({
    mutationFn: async (symptomIds: string[]) => {
      return apiRequest<SymptomCheckResult[]>("POST", "/api/check-symptoms", { symptomIds });
    },
    onError: (error: Error) => {
      toast({
        title: "Error",
        description: error.message || "Failed to analyze symptoms",
        variant: "destructive",
      });
    },
  });

  const filteredSymptoms = symptoms?.filter((symptom) =>
    symptom.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const symptomsByCategory = filteredSymptoms?.reduce((acc, symptom) => {
    if (!acc[symptom.category]) {
      acc[symptom.category] = [];
    }
    acc[symptom.category].push(symptom);
    return acc;
  }, {} as Record<string, Symptom[]>);

  const toggleSymptom = (symptomId: string) => {
    setSelectedSymptoms((prev) =>
      prev.includes(symptomId)
        ? prev.filter((id) => id !== symptomId)
        : [...prev, symptomId]
    );
  };

  const handleAnalyze = () => {
    if (selectedSymptoms.length === 0) {
      toast({
        title: "No Symptoms Selected",
        description: "Please select at least one symptom to analyze",
        variant: "destructive",
      });
      return;
    }
    checkSymptomsMutation.mutate(selectedSymptoms);
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case "low":
        return "text-green-600";
      case "moderate":
        return "text-yellow-600";
      case "high":
        return "text-red-600";
      default:
        return "text-gray-600";
    }
  };

  return (
    <div className="min-h-screen py-8 md:py-12">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-4" data-testid="page-title">Symptom Checker</h1>
          <p className="text-lg text-muted-foreground leading-relaxed max-w-3xl">
            Select the symptoms you're experiencing to receive educational information about possible conditions
          </p>
        </div>

        <div className="mb-8">
          <DisclaimerBanner />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
          {/* Selection Panel */}
          <div className="lg:col-span-3 space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Select Your Symptoms</CardTitle>
                <CardDescription>Choose all symptoms that apply to your current condition</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                {/* Search */}
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                  <Input
                    type="search"
                    placeholder="Search symptoms..."
                    className="pl-10"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    data-testid="input-search-symptoms"
                  />
                </div>

                {/* Selected Symptoms */}
                {selectedSymptoms.length > 0 && (
                  <div className="space-y-2">
                    <p className="text-sm font-medium">Selected ({selectedSymptoms.length}):</p>
                    <div className="flex flex-wrap gap-2">
                      {selectedSymptoms.map((id) => {
                        const symptom = symptoms?.find((s) => s.id === id);
                        return (
                          <Badge
                            key={id}
                            variant="secondary"
                            className="gap-1 pr-1"
                            data-testid={`badge-selected-${id}`}
                          >
                            {symptom?.name}
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-4 w-4 p-0 hover:bg-transparent"
                              onClick={() => toggleSymptom(id)}
                              data-testid={`button-remove-${id}`}
                            >
                              <X className="h-3 w-3" />
                            </Button>
                          </Badge>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* Symptom Categories */}
                {symptomsLoading ? (
                  <div className="space-y-4">
                    {[1, 2, 3].map((i) => (
                      <Skeleton key={i} className="h-12 w-full" />
                    ))}
                  </div>
                ) : (
                  <Accordion type="multiple" className="w-full" defaultValue={Object.keys(symptomsByCategory || {})[0] ? [Object.keys(symptomsByCategory || {})[0]] : []}>
                    {Object.entries(symptomsByCategory || {}).map(([category, categorySymptoms]) => (
                      <AccordionItem key={category} value={category}>
                        <AccordionTrigger className="text-base font-semibold" data-testid={`accordion-${category.toLowerCase().replace(/\s+/g, "-")}`}>
                          {category} ({categorySymptoms.length})
                        </AccordionTrigger>
                        <AccordionContent>
                          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                            {categorySymptoms.map((symptom) => (
                              <div
                                key={symptom.id}
                                className="flex items-start space-x-3 hover-elevate rounded-lg p-3 cursor-pointer"
                                onClick={() => toggleSymptom(symptom.id)}
                                data-testid={`symptom-${symptom.id}`}
                              >
                                <Checkbox
                                  id={symptom.id}
                                  checked={selectedSymptoms.includes(symptom.id)}
                                  onCheckedChange={() => toggleSymptom(symptom.id)}
                                  className="mt-0.5"
                                />
                                <label
                                  htmlFor={symptom.id}
                                  className="text-sm leading-relaxed cursor-pointer flex-1"
                                >
                                  {symptom.name}
                                </label>
                              </div>
                            ))}
                          </div>
                        </AccordionContent>
                      </AccordionItem>
                    ))}
                  </Accordion>
                )}

                <Button
                  onClick={handleAnalyze}
                  disabled={selectedSymptoms.length === 0 || checkSymptomsMutation.isPending}
                  className="w-full"
                  size="lg"
                  data-testid="button-analyze-symptoms"
                >
                  {checkSymptomsMutation.isPending ? "Analyzing..." : "Analyze Symptoms"}
                </Button>
              </CardContent>
            </Card>
          </div>

          {/* Results Panel */}
          <div className="lg:col-span-2">
            <div className="lg:sticky lg:top-24 space-y-6">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Info className="h-5 w-5 text-primary" />
                    Analysis Results
                  </CardTitle>
                  <CardDescription>Possible conditions based on selected symptoms</CardDescription>
                </CardHeader>
                <CardContent>
                  {!checkSymptomsMutation.data && !checkSymptomsMutation.isPending && (
                    <div className="text-center py-12 text-muted-foreground">
                      <AlertCircle className="h-12 w-12 mx-auto mb-4 opacity-50" />
                      <p className="text-sm">Select symptoms and click "Analyze Symptoms" to see possible conditions</p>
                    </div>
                  )}

                  {checkSymptomsMutation.isPending && (
                    <div className="space-y-4">
                      {[1, 2, 3].map((i) => (
                        <Skeleton key={i} className="h-32 w-full" />
                      ))}
                    </div>
                  )}

                  {checkSymptomsMutation.data && (
                    <div className="space-y-4">
                      {checkSymptomsMutation.data.map((result, index) => (
                        <Card key={index} className="border-2" data-testid={`result-${index}`}>
                          <CardHeader className="pb-3">
                            <div className="flex items-start justify-between gap-4">
                              <div className="flex-1">
                                <CardTitle className="text-lg mb-2">{result.disease.name}</CardTitle>
                                <div className="flex items-center gap-2 mb-2">
                                  <Badge variant={result.disease.severity === "high" ? "destructive" : "secondary"}>
                                    {result.disease.severity} severity
                                  </Badge>
                                  <span className={`text-sm font-semibold ${getSeverityColor(result.disease.severity)}`}>
                                    {result.matchPercentage}% match
                                  </span>
                                </div>
                                <Progress value={result.matchPercentage} className="h-2" />
                              </div>
                            </div>
                          </CardHeader>
                          <CardContent className="space-y-3 text-sm">
                            <div>
                              <p className="font-medium mb-1">Description:</p>
                              <p className="text-muted-foreground leading-relaxed">{result.disease.description}</p>
                            </div>

                            <div>
                              <p className="font-medium mb-1 flex items-center gap-1">
                                <CheckCircle2 className="h-4 w-4 text-green-600" />
                                Matched Symptoms:
                              </p>
                              <div className="flex flex-wrap gap-1">
                                {result.matchedSymptoms.map((symptom) => (
                                  <Badge key={symptom} variant="outline" className="text-xs">
                                    {symptom}
                                  </Badge>
                                ))}
                              </div>
                            </div>

                            {result.disease.treatments.length > 0 && (
                              <div>
                                <p className="font-medium mb-1">Common Treatments:</p>
                                <ul className="list-disc list-inside text-muted-foreground space-y-1">
                                  {result.disease.treatments.map((treatment, i) => (
                                    <li key={i}>{treatment}</li>
                                  ))}
                                </ul>
                              </div>
                            )}

                            <div className="pt-2 border-t">
                              <p className="font-medium text-destructive mb-1">When to Seek Medical Help:</p>
                              <p className="text-muted-foreground leading-relaxed">{result.disease.whenToSeekHelp}</p>
                            </div>
                          </CardContent>
                        </Card>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
