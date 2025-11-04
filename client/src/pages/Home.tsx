import { Activity, Brain, ClipboardList, DollarSign, FileText, MessageSquare } from "lucide-react";
import { Link } from "wouter";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { DisclaimerBanner } from "@/components/DisclaimerBanner";

const modules = [
  {
    icon: ClipboardList,
    title: "Symptom Checker",
    description: "Analyze your symptoms and learn about possible conditions. Get educational information based on your input.",
    path: "/symptoms",
    color: "text-blue-600",
  },
  {
    icon: MessageSquare,
    title: "Health Chatbot",
    description: "Ask health and wellness questions. Get instant answers from our knowledge base on common health topics.",
    path: "/chat",
    color: "text-green-600",
  },
  {
    icon: FileText,
    title: "Test Lookup",
    description: "Discover which medical tests are recommended for specific conditions and which ones to avoid.",
    path: "/tests",
    color: "text-purple-600",
  },
  {
    icon: DollarSign,
    title: "Cost Estimator",
    description: "Get estimated costs for medical treatments and procedures at different hospital types across India.",
    path: "/costs",
    color: "text-orange-600",
  },
  {
    icon: Brain,
    title: "Report Analyzer",
    description: "Input your blood test values and get educational feedback comparing them to standard ranges.",
    path: "/reports",
    color: "text-pink-600",
  },
];

const stats = [
  { label: "Medical Conditions", value: "50+" },
  { label: "Symptoms Tracked", value: "200+" },
  { label: "Health Topics", value: "100+" },
  { label: "Tests Cataloged", value: "75+" },
];

export default function Home() {
  return (
    <div className="flex flex-col min-h-screen">
      {/* Hero Section */}
      <section className="relative bg-gradient-to-b from-primary/5 to-background py-16 md:py-24">
        <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8 text-center">
          <div className="flex justify-center mb-6">
            <Activity className="h-16 w-16 md:h-20 md:w-20 text-primary" data-testid="hero-icon" />
          </div>
          <h1 className="text-4xl md:text-5xl font-bold leading-tight mb-6" data-testid="hero-title">
            SMART Health Guide+
          </h1>
          <p className="text-xl md:text-2xl text-muted-foreground mb-4 leading-relaxed">
            Your AI-Powered Personal Medical Information System
          </p>
          <p className="text-base md:text-lg text-muted-foreground max-w-2xl mx-auto mb-8 leading-relaxed">
            Access educational health information through intelligent symptom analysis, health guidance, test recommendations, cost estimation, and medical report insights.
          </p>
          
          <div className="max-w-2xl mx-auto mb-8">
            <DisclaimerBanner />
          </div>

          {/* Stats Grid */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6 max-w-3xl mx-auto mt-12">
            {stats.map((stat) => (
              <div key={stat.label} className="text-center" data-testid={`stat-${stat.label.toLowerCase().replace(/\s+/g, "-")}`}>
                <div className="text-3xl md:text-4xl font-bold text-primary">{stat.value}</div>
                <div className="text-xs md:text-sm text-muted-foreground mt-1">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Modules Grid */}
      <section className="py-16 md:py-24">
        <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-2xl md:text-3xl font-semibold mb-4">Explore Our Health Tools</h2>
            <p className="text-muted-foreground max-w-2xl mx-auto leading-relaxed">
              Choose from our comprehensive suite of educational health information modules
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8">
            {modules.map((module) => {
              const Icon = module.icon;
              return (
                <Link key={module.path} href={module.path}>
                  <Card className="h-full hover-elevate active-elevate-2 transition-transform cursor-pointer" data-testid={`module-card-${module.title.toLowerCase().replace(/\s+/g, "-")}`}>
                    <CardHeader className="space-y-4">
                      <div className="flex items-center justify-center w-16 h-16 rounded-lg bg-primary/10">
                        <Icon className={`h-8 w-8 ${module.color}`} />
                      </div>
                      <div>
                        <CardTitle className="text-xl">{module.title}</CardTitle>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <CardDescription className="leading-relaxed">
                        {module.description}
                      </CardDescription>
                      <Button variant="ghost" className="mt-4 w-full" data-testid={`button-explore-${module.title.toLowerCase().replace(/\s+/g, "-")}`}>
                        Explore →
                      </Button>
                    </CardContent>
                  </Card>
                </Link>
              );
            })}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-16 md:py-24 bg-muted/30">
        <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-2xl md:text-3xl font-semibold mb-4">How It Works</h2>
            <p className="text-muted-foreground leading-relaxed">
              Simple, intelligent, and educational
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center space-y-4">
              <div className="flex items-center justify-center w-12 h-12 rounded-full bg-primary text-primary-foreground mx-auto font-bold text-xl">
                1
              </div>
              <h3 className="font-semibold text-lg">Input Your Information</h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Select symptoms, ask questions, or enter test values through our easy-to-use interfaces
              </p>
            </div>

            <div className="text-center space-y-4">
              <div className="flex items-center justify-center w-12 h-12 rounded-full bg-primary text-primary-foreground mx-auto font-bold text-xl">
                2
              </div>
              <h3 className="font-semibold text-lg">AI Analysis</h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Our intelligent system processes your input against our comprehensive medical knowledge base
              </p>
            </div>

            <div className="text-center space-y-4">
              <div className="flex items-center justify-center w-12 h-12 rounded-full bg-primary text-primary-foreground mx-auto font-bold text-xl">
                3
              </div>
              <h3 className="font-semibold text-lg">Get Educational Insights</h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Receive detailed, educational information to help you make informed health decisions
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
