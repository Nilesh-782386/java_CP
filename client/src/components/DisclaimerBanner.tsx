import { AlertTriangle } from "lucide-react";
import { Alert, AlertDescription } from "@/components/ui/alert";

export function DisclaimerBanner() {
  return (
    <Alert className="border-destructive/50 bg-destructive/10" data-testid="disclaimer-banner">
      <AlertTriangle className="h-4 w-4 text-destructive" />
      <AlertDescription className="text-sm leading-normal">
        <span className="font-semibold">Educational Purposes Only:</span> This tool provides general health information and is not a substitute for professional medical advice, diagnosis, or treatment. Always consult with a qualified healthcare provider for medical concerns.
      </AlertDescription>
    </Alert>
  );
}
