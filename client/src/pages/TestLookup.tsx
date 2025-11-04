import { useState } from "react";
import { Search, CheckCircle2, XCircle, FileText, Info } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { DisclaimerBanner } from "@/components/DisclaimerBanner";
import { Skeleton } from "@/components/ui/skeleton";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import type { Disease, TestRecommendation } from "@shared/schema";

export default function TestLookup() {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedDisease, setSelectedDisease] = useState<string | null>(null);

  const { data: diseases, isLoading: diseasesLoading } = useQuery<Disease[]>({
    queryKey: ["/api/diseases"],
  });

  const { data: testRecommendation, isLoading: testsLoading } = useQuery<TestRecommendation>({
    queryKey: [`/api/tests/${selectedDisease}`],
    enabled: !!selectedDisease,
  });

  const filteredDiseases = diseases?.filter((disease) =>
    disease.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen py-8 md:py-12">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-4" data-testid="page-title">Test Recommendations</h1>
          <p className="text-lg text-muted-foreground leading-relaxed max-w-3xl">
            Discover which medical tests are recommended for specific conditions and which ones to avoid
          </p>
        </div>

        <div className="mb-8">
          <DisclaimerBanner />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
          {/* Disease Selection */}
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Select a Condition</CardTitle>
                <CardDescription>Choose a medical condition to see test recommendations</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                  <Input
                    type="search"
                    placeholder="Search conditions..."
                    className="pl-10"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    data-testid="input-search-diseases"
                  />
                </div>

                {diseasesLoading ? (
                  <div className="space-y-2">
                    {[1, 2, 3, 4, 5].map((i) => (
                      <Skeleton key={i} className="h-16 w-full" />
                    ))}
                  </div>
                ) : (
                  <div className="space-y-2 max-h-[600px] overflow-y-auto">
                    {filteredDiseases?.map((disease) => (
                      <Card
                        key={disease.id}
                        className={`cursor-pointer transition-all hover-elevate ${
                          selectedDisease === disease.id ? "border-primary border-2" : ""
                        }`}
                        onClick={() => setSelectedDisease(disease.id)}
                        data-testid={`disease-card-${disease.id}`}
                      >
                        <CardHeader className="p-4">
                          <div className="flex items-center justify-between gap-2">
                            <div className="flex-1">
                              <CardTitle className="text-base">{disease.name}</CardTitle>
                              <CardDescription className="text-xs mt-1 line-clamp-2">
                                {disease.description}
                              </CardDescription>
                            </div>
                            <Badge variant={disease.severity === "high" ? "destructive" : "secondary"}>
                              {disease.severity}
                            </Badge>
                          </div>
                        </CardHeader>
                      </Card>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Test Recommendations */}
          <div className="lg:col-span-3">
            {!selectedDisease ? (
              <Card className="h-full flex items-center justify-center min-h-[400px]">
                <CardContent className="text-center py-12">
                  <FileText className="h-16 w-16 mx-auto mb-4 text-muted-foreground opacity-50" />
                  <h3 className="font-semibold text-lg mb-2">No Condition Selected</h3>
                  <p className="text-sm text-muted-foreground max-w-sm mx-auto">
                    Select a medical condition from the list to view recommended and unnecessary tests
                  </p>
                </CardContent>
              </Card>
            ) : testsLoading ? (
              <div className="space-y-6">
                <Skeleton className="h-64 w-full" />
                <Skeleton className="h-64 w-full" />
              </div>
            ) : testRecommendation ? (
              <div className="space-y-6">
                {/* Header */}
                <Card className="border-primary/50">
                  <CardHeader>
                    <CardTitle className="text-2xl">{testRecommendation.diseaseName}</CardTitle>
                    <CardDescription className="text-base">{testRecommendation.reasoning}</CardDescription>
                  </CardHeader>
                </Card>

                {/* Recommended Tests */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-green-600">
                      <CheckCircle2 className="h-6 w-6" />
                      Recommended Tests
                    </CardTitle>
                    <CardDescription>These tests are helpful for diagnosing and monitoring this condition</CardDescription>
                  </CardHeader>
                  <CardContent>
                    {testRecommendation.recommendedTests.length === 0 ? (
                      <p className="text-sm text-muted-foreground">No specific tests recommended</p>
                    ) : (
                      <Accordion type="multiple" className="w-full">
                        {testRecommendation.recommendedTests.map((test) => (
                          <AccordionItem key={test.id} value={test.id}>
                            <AccordionTrigger className="text-left" data-testid={`test-recommended-${test.id}`}>
                              <div className="flex-1 pr-4">
                                <div className="font-semibold">{test.name}</div>
                                <div className="text-sm text-muted-foreground mt-1">{test.costRange}</div>
                              </div>
                            </AccordionTrigger>
                            <AccordionContent className="space-y-3 text-sm">
                              <div>
                                <p className="font-medium mb-1">Description:</p>
                                <p className="text-muted-foreground leading-relaxed">{test.description}</p>
                              </div>
                              <div>
                                <p className="font-medium mb-1">Purpose:</p>
                                <p className="text-muted-foreground leading-relaxed">{test.purpose}</p>
                              </div>
                              {test.preparation && (
                                <div>
                                  <p className="font-medium mb-1 flex items-center gap-1">
                                    <Info className="h-4 w-4 text-primary" />
                                    Preparation:
                                  </p>
                                  <p className="text-muted-foreground leading-relaxed">{test.preparation}</p>
                                </div>
                              )}
                            </AccordionContent>
                          </AccordionItem>
                        ))}
                      </Accordion>
                    )}
                  </CardContent>
                </Card>

                {/* Tests to Avoid */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-destructive">
                      <XCircle className="h-6 w-6" />
                      Tests to Avoid or Unnecessary
                    </CardTitle>
                    <CardDescription>These tests are not recommended or provide limited value for this condition</CardDescription>
                  </CardHeader>
                  <CardContent>
                    {testRecommendation.testsToAvoid.length === 0 ? (
                      <p className="text-sm text-muted-foreground">No tests specifically flagged to avoid</p>
                    ) : (
                      <Accordion type="multiple" className="w-full">
                        {testRecommendation.testsToAvoid.map((test) => (
                          <AccordionItem key={test.id} value={test.id}>
                            <AccordionTrigger className="text-left" data-testid={`test-avoid-${test.id}`}>
                              <div className="flex-1 pr-4">
                                <div className="font-semibold">{test.name}</div>
                                <div className="text-sm text-muted-foreground mt-1">{test.costRange}</div>
                              </div>
                            </AccordionTrigger>
                            <AccordionContent className="space-y-3 text-sm">
                              <div>
                                <p className="font-medium mb-1">Why to avoid:</p>
                                <p className="text-muted-foreground leading-relaxed">{test.description}</p>
                              </div>
                              <div>
                                <p className="font-medium mb-1">Reasoning:</p>
                                <p className="text-muted-foreground leading-relaxed">{test.purpose}</p>
                              </div>
                            </AccordionContent>
                          </AccordionItem>
                        ))}
                      </Accordion>
                    )}
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
