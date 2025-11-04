import { Activity, Heart } from "lucide-react";
import { Link } from "wouter";

export function Footer() {
  return (
    <footer className="border-t bg-muted/30 mt-auto">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-12 md:py-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 md:gap-12">
          {/* About */}
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <Activity className="h-6 w-6 text-primary" />
              <span className="font-bold text-lg">SMART Health Guide+</span>
            </div>
            <p className="text-sm text-muted-foreground leading-relaxed">
              An AI-powered educational health information system providing symptom analysis, health guidance, and medical insights for informational purposes.
            </p>
          </div>

          {/* Quick Links */}
          <div className="space-y-4">
            <h3 className="font-semibold text-sm">Quick Links</h3>
            <nav className="flex flex-col space-y-2">
              <Link href="/symptoms" className="text-sm text-muted-foreground hover:text-foreground transition-colors hover-elevate rounded px-2 py-1 -ml-2">
                Symptom Checker
              </Link>
              <Link href="/chat" className="text-sm text-muted-foreground hover:text-foreground transition-colors hover-elevate rounded px-2 py-1 -ml-2">
                Health Chatbot
              </Link>
              <Link href="/tests" className="text-sm text-muted-foreground hover:text-foreground transition-colors hover-elevate rounded px-2 py-1 -ml-2">
                Test Recommendations
              </Link>
              <Link href="/costs" className="text-sm text-muted-foreground hover:text-foreground transition-colors hover-elevate rounded px-2 py-1 -ml-2">
                Cost Estimator
              </Link>
              <Link href="/reports" className="text-sm text-muted-foreground hover:text-foreground transition-colors hover-elevate rounded px-2 py-1 -ml-2">
                Report Analyzer
              </Link>
            </nav>
          </div>

          {/* Disclaimer */}
          <div className="space-y-4">
            <h3 className="font-semibold text-sm">Important Notice</h3>
            <p className="text-xs text-muted-foreground leading-relaxed">
              This platform is designed for educational and informational purposes only. It does not provide medical advice, diagnosis, or treatment. Always seek the advice of your physician or other qualified health provider.
            </p>
          </div>
        </div>

        <div className="mt-8 pt-8 border-t">
          <div className="flex flex-col sm:flex-row justify-between items-center gap-4">
            <p className="text-xs text-muted-foreground">
              © 2025 SMART Health Guide+. All rights reserved.
            </p>
            <p className="text-xs text-muted-foreground flex items-center gap-1">
              Made with <Heart className="h-3 w-3 text-destructive fill-destructive" /> for education
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
}
