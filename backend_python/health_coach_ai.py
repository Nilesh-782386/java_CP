from datetime import datetime
from typing import Dict, List, Any


class HealthCoachAI:
    def __init__(self):
        self.intervention_templates = self._load_intervention_templates()

    def _load_intervention_templates(self) -> Dict[str, Dict[str, str]]:
        return {
            'nutrition': {
                'LOW': "Maintain balanced diet with whole foods. Focus on portion control and regular meal timing.",
                'MODERATE': "Reduce processed foods and sugar intake. Increase fiber-rich vegetables and lean proteins.",
                'HIGH': "Strict carbohydrate management. Consult nutritionist for personalized meal planning. Monitor blood sugar regularly.",
                'VERY_HIGH': "Immediate dietary intervention needed. Medical nutrition therapy recommended. Daily food logging essential."
            },
            'exercise': {
                'LOW': "30 minutes moderate activity 5 days/week. Include walking, cycling, or swimming.",
                'MODERATE': "45 minutes cardio + strength training 5 days/week. Include interval training 2x/week.",
                'HIGH': "60 minutes daily structured exercise. Supervised cardiac rehab if indicated. Monitor exertion levels.",
                'VERY_HIGH': "Medically supervised exercise program only. Gradual intensity progression with continuous monitoring."
            },
            'stress_management': {
                'LOW': "Daily mindfulness practice 10 minutes. Maintain work-life balance.",
                'MODERATE': "Stress reduction techniques 2x/day. Consider counseling for stress management.",
                'HIGH': "Structured stress management program. Regular therapy sessions. Limit work hours.",
                'VERY_HIGH': "Immediate stress intervention needed. Medical leave consideration. Daily relaxation practice essential."
            },
            'monitoring': {
                'LOW': "Annual health checkups. Basic lifestyle tracking.",
                'MODERATE': "Quarterly health assessments. Home monitoring of key metrics.",
                'HIGH': "Monthly doctor visits. Continuous health parameter tracking.",
                'VERY_HIGH': "Weekly medical supervision. Emergency action plan. 24/7 symptom monitoring."
            }
        }

    def generate_personalized_plan(self, user_profile: Dict[str, Any], risk_predictions: Dict[str, Any]) -> Dict[str, Any]:
        primary_risk = self._identify_primary_risk(risk_predictions)
        health_score = user_profile.get('health_score', 65)

        plan = {
            'primary_condition': primary_risk,
            'summary': {
                'text': self._generate_summary(primary_risk, health_score)
            },
            'weekly_schedule': self._generate_weekly_schedule(primary_risk),
            'nutrition_plan': self._generate_nutrition_plan(primary_risk),
            'exercise_routine': self._generate_exercise_plan(primary_risk, user_profile),
            'monitoring_plan': self._generate_monitoring_plan(primary_risk),
            'milestones': self._generate_milestones(),
            'risk_reduction_targets': self._calculate_risk_reduction(risk_predictions),
            'generated_at': datetime.now().isoformat()
        }

        return plan

    def _identify_primary_risk(self, predictions: Dict[str, Any]) -> str:
        max_risk = 0
        primary_condition = 'general_health'
        for condition, data in predictions.items():
            risk_percent = data.get('risk_percentage', 0)
            if risk_percent > max_risk:
                max_risk = risk_percent
                primary_condition = condition
        return primary_condition

    def _generate_summary(self, primary_risk: str, health_score: int) -> str:
        condition_names = {
            'diabetes': 'diabetes',
            'heart_disease': 'heart disease',
            'hypertension': 'high blood pressure',
            'general_health': 'general health improvement'
        }

        condition = condition_names.get(primary_risk, 'health improvement')

        return (
            f"Based on your health assessment (score: {health_score}/100), this 30-day plan focuses on reducing your "
            f"{condition} risk. The program includes targeted nutrition, exercise, and monitoring strategies designed "
            f"for sustainable improvement. Follow this plan consistently to see measurable changes in your health metrics."
        )

    def _generate_weekly_schedule(self, primary_risk: str) -> List[Dict[str, Any]]:
        base_schedule = [
            {"day": "Monday", "focus": "Cardio Foundation", "activities": ["30min brisk walking", "Meal prep", "Hydration tracking"]},
            {"day": "Tuesday", "focus": "Strength & Nutrition", "activities": ["Bodyweight exercises", "Protein-focused meals", "Stress check"]},
            {"day": "Wednesday", "focus": "Active Recovery", "activities": ["Yoga/stretching", "Vegetable-rich meals", "Sleep quality review"]},
            {"day": "Thursday", "focus": "Cardio Intensity", "activities": ["Interval training", "Hydration focus", "Progress measurement"]},
            {"day": "Friday", "focus": "Strength & Planning", "activities": ["Resistance training", "Weekend meal planning", "Weekly review"]},
            {"day": "Saturday", "focus": "Active Lifestyle", "activities": ["Outdoor activity", "Family healthy cooking", "Relaxation techniques"]},
            {"day": "Sunday", "focus": "Recovery & Prep", "activities": ["Light walking", "Weekly food prep", "Next week planning"]}
        ]

        if primary_risk == 'diabetes':
            for day in base_schedule:
                day['activities'].append("Blood sugar monitoring")
        elif primary_risk == 'heart_disease':
            for day in base_schedule:
                if "Cardio" in day['focus']:
                    day['activities'].append("Heart rate monitoring")
        elif primary_risk == 'hypertension':
            for day in base_schedule:
                day['activities'].append("Blood pressure logging")

        return base_schedule

    def _generate_nutrition_plan(self, primary_risk: str) -> Dict[str, Any]:
        plan = {
            'breakfast': "High-protein meal with complex carbs",
            'lunch': "Lean protein with colorful vegetables",
            'dinner': "Light protein with fibrous vegetables",
            'snacks': "Two healthy snacks between meals",
            'hydration': "8-10 glasses of water daily",
            'avoid': "Processed foods, sugary drinks, excessive salt"
        }

        if primary_risk == 'diabetes':
            plan.update({
                'carbohydrates': "Distribute evenly throughout the day",
                'timing': "Eat every 3-4 hours to maintain blood sugar",
                'special_notes': "Focus on low glycemic index foods. Monitor portion sizes."
            })
        elif primary_risk == 'heart_disease':
            plan.update({
                'fats': "Emphasise unsaturated fats, avoid trans fats",
                'sodium': "Limit to 1500mg daily",
                'special_notes': "Increase omega-3s and cholesterol-lowering foods (oats, nuts)."
            })
        elif primary_risk == 'hypertension':
            plan.update({
                'sodium': "Limit to 1500mg daily, increase potassium intake",
                'special_notes': "Follow DASH diet principles. Emphasise fruits, vegetables, low-fat dairy."
            })

        return plan

    def _generate_exercise_plan(self, primary_risk: str, user_profile: Dict[str, Any]) -> Dict[str, Any]:
        plan = {
            'frequency': "5 days per week",
            'duration': "30-45 minutes per session",
            'warmup': "5-10 minutes dynamic stretching",
            'cooldown': "5-10 minutes static stretching",
            'progression': "Increase intensity weekly"
        }

        age = user_profile.get('age', 40) or 40

        if primary_risk == 'diabetes':
            plan.update({
                'cardio': "Brisk walking, cycling, or swimming",
                'strength': "Full body resistance training twice weekly",
                'special_notes': "Consider exercising after meals to manage blood sugar."
            })
        elif primary_risk == 'heart_disease':
            plan.update({
                'cardio': "Gradual progression of walking or stationary cycling",
                'strength': "Light weights with higher repetitions",
                'special_notes': "Stay within a safe heart rate zone; stop if chest pain occurs."
            })
        elif primary_risk == 'hypertension':
            plan.update({
                'cardio': "Moderate-intensity cardio (walking, swimming)",
                'strength': "Low-impact strength training 2-3x/week",
                'special_notes': "Avoid heavy straining; incorporate breathing techniques."
            })
        else:
            plan.update({
                'cardio': "Mix of steady-state and interval training",
                'strength': "Full body workouts 3x/week"
            })

        if age > 60:
            plan['special_notes'] = "Focus on balance and mobility. Prioritise low-impact activities."

        return plan

    def _generate_monitoring_plan(self, primary_risk: str) -> Dict[str, str]:
        monitoring = {
            'weight': "Track weekly",
            'sleep_quality': "Log hours and quality nightly",
            'energy_levels': "Rate daily on a scale of 1-10"
        }

        if primary_risk == 'diabetes':
            monitoring['blood_sugar'] = "Monitor fasting and post-meal levels"
            monitoring['hba1c'] = "Test quarterly"
        elif primary_risk == 'heart_disease':
            monitoring['blood_pressure'] = "Check daily"
            monitoring['heart_rate'] = "Track resting and during exercise"
            monitoring['cholesterol'] = "Test quarterly"
        elif primary_risk == 'hypertension':
            monitoring['blood_pressure'] = "Track twice daily"
            monitoring['sodium_intake'] = "Log daily"

        return monitoring

    def _generate_milestones(self) -> List[Dict[str, Any]]:
        return [
            {"week": 1, "goal": "Establish routine", "metrics": ["80% schedule adherence", "Improved hydration"]},
            {"week": 2, "goal": "Build consistency", "metrics": ["90% schedule adherence", "Energy level improvement"]},
            {"week": 3, "goal": "Measure progress", "metrics": ["Health score +5 points", "Key metric improvement"]},
            {"week": 4, "goal": "Sustainable habits", "metrics": ["Full routine mastery", "5% risk reduction target"]}
        ]

    def _calculate_risk_reduction(self, predictions: Dict[str, Any]) -> Dict[str, Any]:
        targets = {}
        for condition, data in predictions.items():
            current_risk = data.get('risk_percentage', 0)
            if current_risk <= 0:
                continue
            achievable_reduction = min(15.0, current_risk * 0.3)
            targets[condition] = {
                'current_risk': round(current_risk, 1),
                'target_reduction': round(achievable_reduction, 1),
                'new_target_risk': round(max(0.0, current_risk - achievable_reduction), 1),
                'timeframe': "30 days with full adherence"
            }
        return targets


health_coach = HealthCoachAI()

