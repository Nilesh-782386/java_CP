import { Switch, Route } from "wouter";
import { queryClient } from "./lib/queryClient";
import { QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import Home from "@/pages/Home";
import SymptomChecker from "@/pages/SymptomChecker";
import HealthChatbot from "@/pages/HealthChatbot";
import TestLookup from "@/pages/TestLookup";
import CostEstimator from "@/pages/CostEstimator";
import ReportAnalyzer from "@/pages/ReportAnalyzer";
import NotFound from "@/pages/not-found";

function Router() {
  return (
    <Switch>
      <Route path="/" component={Home} />
      <Route path="/symptoms" component={SymptomChecker} />
      <Route path="/chat" component={HealthChatbot} />
      <Route path="/tests" component={TestLookup} />
      <Route path="/costs" component={CostEstimator} />
      <Route path="/reports" component={ReportAnalyzer} />
      <Route component={NotFound} />
    </Switch>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-1">
            <Router />
          </main>
          <Footer />
        </div>
        <Toaster />
      </TooltipProvider>
    </QueryClientProvider>
  );
}

export default App;
