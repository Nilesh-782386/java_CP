import numpy as np
import pandas as pd
from sklearn.ensemble import VotingClassifier
import xgboost as xgb
from lightgbm import LGBMClassifier
from sklearn.neural_network import MLPClassifier
import joblib


class AdvancedRiskPredictor:
    def __init__(self):
        self.ensemble_model = None
        self.diseases = ['diabetes', 'heart_disease', 'hypertension']
        self.calibrated_models = {}
        self.load_or_train_models()

    def load_or_train_models(self):
        """Load pre-trained models or initialize ensemble."""
        try:
            self.ensemble_model = joblib.load('models/ensemble_risk_model.pkl')
            for disease in self.diseases:
                try:
                    self.calibrated_models[disease] = joblib.load(
                        f'models/{disease}_calibrated.pkl'
                    )
                except Exception:
                    pass
            print("Loaded pre-trained ensemble model")
        except Exception:
            print("No pre-trained model found. Using default ensemble configuration.")
            self.ensemble_model = self.create_ensemble()

    def create_ensemble(self):
        """Create weighted ensemble model."""
        estimators = [
            ('xgb', xgb.XGBClassifier(
                n_estimators=100,
                max_depth=6,
                learning_rate=0.1,
                subsample=0.8,
                eval_metric='logloss',
                random_state=42
            )),
            
            ('lgb', L+GBMClassifier(
                n_estimators=100,
                max_depth=6,
                learning_rate=0.1,
                num_leaves=31,
                random_state=42
            )),
            ('nn', MLPClassifier(
                hidden_layer_sizes=(64, 32),
                activation='relu',
                learning_rate_init=0.001,
                max_iter=500,
                early_stopping=True,
                random_state=42
            ))
        ]
        return VotingClassifier(
            estimators=estimators,
            voting='soft',
            weights=[0.4, 0.4, 0.2]
        )

    def predict_with_confidence(self, user_data):
        """Enhanced prediction with confidence intervals and lifestyle intelligence."""
        results = {
            'predictions': {},
            'lifestyle_summary': {},
            'disease_impacts': {},
            'data_quality': {}
        }

        if not isinstance(user_data, pd.DataFrame):
            user_data = pd.DataFrame([user_data])

        if user_data.empty:
            return results

        row = user_data.iloc[0]
        lifestyle_summary = self.analyze_lifestyle(row)
        disease_impacts = self.calculate_disease_impacts(row, lifestyle_summary)

        results['lifestyle_summary'] = lifestyle_summary
        results['disease_impacts'] = disease_impacts

        data_quality = {
            'missing_features': int(user_data.isna().sum().sum()),
            'total_features': int(user_data.shape[1]),
            'completeness': round(
                100.0 * (1.0 - (user_data.isna().sum().sum() / max(user_data.size, 1))), 1
            )
        }
        results['data_quality'] = data_quality

        for disease in self.diseases:
            try:
                model = self.ensemble_model
                calibrated_model = self.calibrated_models.get(disease)

                probability = None
                fallback_used = False

                if calibrated_model is not None:
                    try:
                        probability = float(calibrated_model.predict_proba(user_data)[:, 1][0])
                    except Exception:
                        probability = None
                elif model is not None and hasattr(model, "estimators_"):
                    try:
                        probability = float(model.predict_proba(user_data)[:, 1][0])
                    except Exception:
                        probability = None

                if probability is None:
                    probability = self._fallback_probability(user_data, disease)
                    fallback_used = True

                probability = max(0.0, min(1.0, probability))
                base_risk_percentage = probability * 100.0

                adjustment = disease_impacts.get(disease, {}).get('adjustment', 0.0)
                risk_percentage = self._clamp_percentage(base_risk_percentage + adjustment)

                confidence = self.calculate_confidence_interval(probability, fallback_used=fallback_used)
                uncertainty_score = self.calculate_uncertainty(user_data, fallback_used=fallback_used)

                disease_result = {
                    'risk_percentage': round(risk_percentage, 1),
                    'base_risk_percentage': round(base_risk_percentage, 1),
                    'confidence_interval': confidence,
                    'uncertainty_score': round(uncertainty_score, 3),
                    'risk_category': self.categorize_risk(risk_percentage, uncertainty_score),
                    'lifestyle_adjustment': disease_impacts.get(disease, {})
                }
                results['predictions'][disease] = disease_result
            except Exception as exc:
                print(f"Error predicting {disease}: {exc}")
                results['predictions'][disease] = {
                    'risk_percentage': 0.0,
                    'base_risk_percentage': 0.0,
                    'confidence_interval': {'lower': 0.0, 'upper': 0.0, 'width': 0.0},
                    'uncertainty_score': 1.0,
                    'risk_category': 'UNCERTAIN',
                    'lifestyle_adjustment': {
                        'adjustment': 0.0,
                        'drivers': ['Model error encountered'],
                        'protective': [],
                        'priority_action': None
                    }
                }

        results['overall_focus'] = lifestyle_summary.get('priority_actions', [])
        results['protective_factors'] = lifestyle_summary.get('protective_factors', [])
        return results

    def calculate_confidence_interval(self, probability, fallback_used=False, n_samples=1000):
        """Calculate confidence intervals using bootstrap approximation."""
        if fallback_used:
            margin = 0.15  # Wider interval when using fallback heuristics
        else:
            std_error = np.sqrt(probability * (1 - probability) / max(n_samples, 1))
            margin = 1.96 * std_error

        lower = max(0.0, (probability - margin) * 100.0)
        upper = min(100.0, (probability + margin) * 100.0)

        return {
            'lower': round(lower, 1),
            'upper': round(upper, 1),
            'width': round(upper - lower, 1)
        }

    def calculate_uncertainty(self, user_data, fallback_used=False):
        """Calculate model uncertainty based on data completeness."""
        if isinstance(user_data, pd.DataFrame):
            total_cells = user_data.shape[0] * user_data.shape[1]
            if total_cells == 0:
                return 1.0
            missing = user_data.isna().sum().sum()
            missing_ratio = missing / total_cells
            uncertainty = min(1.0, 0.1 + missing_ratio)
            if fallback_used:
                uncertainty = max(uncertainty, 0.55)
            return uncertainty
        return 0.3 if not fallback_used else 0.55

    def _fallback_probability(self, user_data, disease):
        """Use baseline risk or heuristics when ensemble predictions are unavailable."""
        try:
            if isinstance(user_data, pd.DataFrame) and not user_data.empty:
                row = user_data.iloc[0]
                baseline_column = f'baseline_{disease}_risk'
                if baseline_column in row.index:
                    value = row[baseline_column]
                    if pd.notna(value):
                        value = float(value)
                        if value > 1.0:
                            value /= 100.0
                        return max(0.0, min(1.0, value))

                health_score = row.get('baseline_health_score')
                if health_score is not None and pd.notna(health_score):
                    health_score = float(health_score)
                    if health_score > 0:
                        probability = max(0.0, min(1.0, (100.0 - health_score) / 100.0))
                        return probability
        except Exception:
            pass

        return 0.25

    def categorize_risk(self, risk_percentage, uncertainty_score):
        """Categorize risk with uncertainty consideration."""
        if uncertainty_score > 0.3:
            return "UNCERTAIN"
        if risk_percentage < 20:
            return "LOW"
        if risk_percentage < 50:
            return "MODERATE"
        if risk_percentage < 80:
            return "HIGH"
        return "VERY_HIGH"

    def analyze_lifestyle(self, row):
        """Generate holistic lifestyle assessment from user inputs."""
        categories = {
            'nutrition': self._score_nutrition(row),
            'activity': self._score_activity(row),
            'sleep': self._score_sleep(row),
            'stress': self._score_stress(row),
            'substance_use': self._score_substance_use(row),
            'medication': self._score_medication(row),
            'preventive': self._score_preventive(row),
            'environment': self._score_environment(row)
        }

        valid_scores = [cat['score'] for cat in categories.values() if cat['score'] is not None]
        overall_score = float(np.mean(valid_scores)) if valid_scores else 60.0
        overall_status = self._score_status(overall_score)

        priority_actions = []
        risk_drivers = []
        protective_factors = []

        for key, data in sorted(categories.items(), key=lambda item: item[1]['score'] or 0):
            label = data['label']
            score = data['score']
            status = data['status']
            if score is None:
                continue

            if score < 75 and data['actions']:
                priority_actions.append({
                    'category': key,
                    'label': label,
                    'recommended_action': data['actions'][0],
                    'score': score,
                    'status': status
                })

            if data['risks']:
                risk_drivers.append({
                    'category': key,
                    'label': label,
                    'insight': data['risks'][0],
                    'score': score
                })

            if data['positives']:
                protective_factors.append({
                    'category': key,
                    'label': label,
                    'insight': data['positives'][0],
                    'score': score
                })

        return {
            'overall_score': round(overall_score, 1),
            'overall_status': overall_status,
            'categories': categories,
            'priority_actions': priority_actions[:5],
            'risk_drivers': risk_drivers[:6],
            'protective_factors': protective_factors[:6]
        }

    def calculate_disease_impacts(self, row, lifestyle_summary):
        """Translate lifestyle patterns into disease-specific adjustments."""
        categories = lifestyle_summary.get('categories', {})
        nutrition = categories.get('nutrition', {})
        activity = categories.get('activity', {})
        sleep = categories.get('sleep', {})
        stress = categories.get('stress', {})
        substance = categories.get('substance_use', {})
        medication = categories.get('medication', {})
        preventive = categories.get('preventive', {})
        environment = categories.get('environment', {})

        impacts = {}

        for disease in self.diseases:
            adjustment = 0.0
            drivers = []
            protective = []

            def apply_penalty(condition, amount, message):
                nonlocal adjustment
                if condition:
                    adjustment += amount
                    drivers.append(message)

            def apply_credit(condition, amount, message):
                nonlocal adjustment
                if condition:
                    adjustment -= amount
                    protective.append(message)

            # Nutrition impacts
            nutrition_score = nutrition.get('score', 70)
            if disease in ('diabetes', 'heart_disease'):
                apply_penalty(nutrition_score < 50, 8.0, "Nutrition habits increase metabolic strain.")
                apply_penalty(nutrition_score < 35, 6.0, "Very limited whole foods intake.")
                apply_credit(nutrition_score >= 80, 4.0, "Balanced nutrition supports metabolic resilience.")

            # Activity impacts
            activity_score = activity.get('score', 70)
            apply_penalty(activity_score < 45, 7.0, "Insufficient weekly activity and high sedentary time.")
            apply_penalty(activity_score < 30, 6.0, "Severely low physical activity.")
            apply_credit(activity_score >= 80, 4.5, "Consistent activity provides protective effect.")

            # Sleep impacts
            sleep_score = sleep.get('score', 70)
            apply_penalty(sleep_score < 50, 5.0, "Sleep quality concerns are elevating baseline risk.")
            apply_penalty(sleep_score < 35, 4.0, "Significant sleep disruption detected.")
            apply_credit(sleep_score >= 80, 3.5, "Restorative sleep improves cardio-metabolic control.")

            # Stress impacts
            stress_score = stress.get('score', 70)
            apply_penalty(stress_score < 50, 5.0, "Stress load and coping capacity are impacting resilience.")
            apply_penalty(stress_score < 35, 4.0, "Sustained high stress may destabilize health markers.")
            apply_credit(stress_score >= 80, 3.0, "Healthy stress coping protects long-term health.")

            # Substance use impacts
            substance_score = substance.get('score', 70)
            apply_penalty(substance_score < 55, 6.0, "Substance use patterns elevate vascular and metabolic risk.")
            apply_penalty(substance_score < 35, 6.0, "High exposure to tobacco/alcohol/caffeine.")
            apply_credit(substance_score >= 80, 3.5, "Limited substance exposure supports cardiovascular stability.")

            # Medication adherence impacts (especially for chronic disease control)
            medication_score = medication.get('score', 75)
            if disease in ('diabetes', 'hypertension'):
                apply_penalty(medication_score < 60, 6.0, "Medication adherence gaps can destabilize control.")
                apply_credit(medication_score >= 85, 3.0, "Medication adherence supports steady control.")

            # Preventive care & environment
            preventive_score = preventive.get('score', 70)
            environment_score = environment.get('score', 70)
            apply_penalty(preventive_score < 55, 3.5, "Preventive care gaps reduce early detection.")
            apply_penalty(environment_score < 55, 3.0, "Work or environmental exposure adds strain.")
            apply_credit(preventive_score >= 85, 2.5, "Preventive check-ups support early course correction.")

            adjustment = round(self._clamp_adjustment(adjustment), 1)
            priority_action = None

            for item in lifestyle_summary.get('priority_actions', []):
                if item['category'] in ('nutrition', 'activity', 'sleep', 'stress', 'substance_use'):
                    priority_action = item['recommended_action']
                    break
            if priority_action is None and lifestyle_summary.get('priority_actions'):
                priority_action = lifestyle_summary['priority_actions'][0]['recommended_action']

            impacts[disease] = {
                'adjustment': adjustment,
                'drivers': drivers[:4],
                'protective': protective[:4],
                'priority_action': priority_action
            }

        return impacts

    def _score_status(self, score):
        if score is None:
            return "UNKNOWN"
        if score >= 80:
            return "Optimal"
        if score >= 65:
            return "On Track"
        if score >= 50:
            return "Needs Attention"
        return "High Priority"

    def _safe_number(self, value, default=None):
        if value is None:
            return default
        try:
            if isinstance(value, str) and not value.strip():
                return default
            if isinstance(value, (list, dict)):
                return default
            if pd.isna(value):
                return default
        except Exception:
            return default
        try:
            return float(value)
        except (ValueError, TypeError):
            return default

    def _score_nutrition(self, row):
        score = 70.0
        risks = []
        positives = []
        actions = []

        diet_quality = self._safe_number(row.get('diet_quality'), 1)
        vegetable_servings = self._safe_number(row.get('vegetable_servings'), None)
        processed_meals = self._safe_number(row.get('processed_meals_per_week'), None)
        hydration = self._safe_number(row.get('hydration_glasses'), None)
        caffeine = self._safe_number(row.get('caffeine_intake'), None)
        alcohol = self._safe_number(row.get('alcohol'), None)

        if diet_quality is not None:
            if diet_quality <= 0:
                score -= 18
                risks.append("Diet quality is flagged as poor with limited whole foods.")
                actions.append("Aim for balanced meals rich in vegetables, lean protein, and whole grains.")
            elif diet_quality == 1:
                score -= 8
                actions.append("Increase vegetables and fiber-rich foods across meals.")
            elif diet_quality >= 2:
                score += 6
                positives.append("Balanced diet quality reported.")

        if vegetable_servings is not None:
            if vegetable_servings < 3:
                score -= 10
                risks.append("Vegetable intake falls below daily recommendations.")
                actions.append("Target 4-5 servings of vegetables and fruits per day.")
            elif vegetable_servings >= 5:
                score += 6
                positives.append("Strong intake of vegetables and fruits.")

        if processed_meals is not None:
            if processed_meals >= 6:
                score -= 12
                risks.append("High reliance on processed or takeaway meals.")
                actions.append("Swap processed meals with home-prepared options at least twice per week.")
            elif processed_meals <= 2:
                score += 4
                positives.append("Minimal reliance on processed meals.")

        if hydration is not None:
            if hydration < 5:
                score -= 6
                actions.append("Increase plain water intake to 7-8 glasses daily.")
            elif hydration >= 8:
                score += 4
                positives.append("Hydration habits meet daily fluid needs.")

        if caffeine is not None:
            if caffeine >= 3:
                score -= 4
                actions.append("Limit caffeine-heavy beverages in the afternoon.")

        if alcohol is not None and alcohol >= 2:
            score -= 6
            risks.append("Alcohol intake is in the high range.")
            actions.append("Limit alcohol to social occasions (≤2 drinks/week).")

        score = float(np.clip(score, 5.0, 100.0))
        return {
            'label': 'Nutrition & Hydration',
            'score': round(score, 1),
            'status': self._score_status(score),
            'risks': self._unique_messages(risks),
            'positives': self._unique_messages(positives),
            'actions': self._unique_messages(actions)
        }

    def _score_activity(self, row):
        score = 65.0
        risks = []
        positives = []
        actions = []

        moderate_minutes = self._safe_number(row.get('moderate_activity_minutes'), 0) or 0
        vigorous_minutes = self._safe_number(row.get('vigorous_activity_minutes'), 0) or 0
        strength_sessions = self._safe_number(row.get('strength_training_sessions'), 0) or 0
        sedentary_hours = self._safe_number(row.get('sedentary_hours'), None)
        exercise_level = self._safe_number(row.get('exercise'), None)

        total_activity = moderate_minutes + (vigorous_minutes * 2)
        meets_guidelines = total_activity >= 150

        if meets_guidelines:
            score += 6
            positives.append("Weekly activity volume meets or exceeds recommended levels.")
        else:
            shortfall = max(0, 150 - total_activity)
            penalty = min(12, shortfall * 0.05)
            score -= penalty
            actions.append("Accumulate at least 150 minutes of moderate activity weekly.")

        if strength_sessions >= 2:
            score += 4
            positives.append("Strength training is included twice weekly.")
        else:
            actions.append("Add 2 sessions of strength or resistance training each week.")

        if sedentary_hours is not None:
            if sedentary_hours >= 9:
                score -= 10
                risks.append("Daily sedentary time exceeds 9 hours.")
                actions.append("Take movement breaks every hour to counter long sitting periods.")
            elif sedentary_hours <= 6:
                score += 3
                positives.append("Sedentary time is kept within a healthy range.")

        if exercise_level is not None:
            if exercise_level == 0:
                score -= 6
                actions.append("Start with light walks or stretching 3-4 days weekly.")
            elif exercise_level == 2:
                score += 3

        score = float(np.clip(score, 5.0, 100.0))
        return {
            'label': 'Physical Activity & Movement',
            'score': round(score, 1),
            'status': self._score_status(score),
            'risks': self._unique_messages(risks),
            'positives': self._unique_messages(positives),
            'actions': self._unique_messages(actions)
        }

    def _score_sleep(self, row):
        score = 68.0
        risks = []
        positives = []
        actions = []

        sleep_hours = self._safe_number(row.get('sleep_hours'), None)
        sleep_quality = self._safe_number(row.get('sleep_quality'), None)
        sleep_consistency = self._safe_number(row.get('sleep_consistency'), None)
        snoring = self._safe_number(row.get('snoring'), 0) or 0

        if sleep_hours is not None:
            if sleep_hours < 6:
                score -= 10
                risks.append("Sleep duration is below 6 hours on most nights.")
                actions.append("Target 7-8 hours of consistent sleep to support recovery.")
            elif sleep_hours > 9:
                score -= 4
                actions.append("Assess for fragmented sleep causing long total time in bed.")
            else:
                score += 4
                positives.append("Sleep duration is within the optimal range.")

        if sleep_quality is not None:
            if sleep_quality <= 1:
                score -= 10
                actions.append("Practice a calming wind-down routine and limit screen time before bed.")
            elif sleep_quality >= 3:
                score += 4
                positives.append("Sleep quality is reported as refreshing.")

        if sleep_consistency is not None:
            if sleep_consistency <= 1:
                score -= 6
                actions.append("Keep consistent sleep and wake times throughout the week.")
            elif sleep_consistency >= 3:
                score += 4

        if snoring >= 1:
            score -= 4
            risks.append("Snoring or possible breathing disruptions noted.")
            actions.append("Discuss snoring or apnea screening with a healthcare provider.")

        score = float(np.clip(score, 5.0, 100.0))
        return {
            'label': 'Sleep & Recovery',
            'score': round(score, 1),
            'status': self._score_status(score),
            'risks': self._unique_messages(risks),
            'positives': self._unique_messages(positives),
            'actions': self._unique_messages(actions)
        }

    def _score_stress(self, row):
        score = 66.0
        risks = []
        positives = []
        actions = []

        stress_level = self._safe_number(row.get('stress_level'), 1)
        stress_coping = self._safe_number(row.get('stress_coping'), None)
        work_hours = self._safe_number(row.get('work_hours'), None)
        mood_stability = self._safe_number(row.get('mood_stability'), None)

        if stress_level is not None:
            if stress_level >= 2:
                score -= 10
                risks.append("High perceived stress reported.")
                actions.append("Schedule recovery breaks and stress-relief practices daily.")
            elif stress_level == 0:
                score += 4

        if stress_coping is not None:
            if stress_coping <= 1:
                score -= 8
                actions.append("Build a support plan: relaxation, journaling, or guided breathing.")
            elif stress_coping >= 3:
                score += 4
                positives.append("Effective coping strategies reported.")

        if work_hours is not None and work_hours > 55:
            score -= 6
            actions.append("Protect time off; consider micro-breaks to reduce overload.")

        if mood_stability is not None:
            if mood_stability <= 1:
                score -= 6
                actions.append("Consult with a counselor if mood swings persist.")
            elif mood_stability >= 3:
                score += 3

        score = float(np.clip(score, 5.0, 100.0))
        return {
            'label': 'Stress & Mental Wellbeing',
            'score': round(score, 1),
            'status': self._score_status(score),
            'risks': self._unique_messages(risks),
            'positives': self._unique_messages(positives),
            'actions': self._unique_messages(actions)
        }

    def _score_substance_use(self, row):
        score = 72.0
        risks = []
        positives = []
        actions = []

        smoking = bool(self._safe_number(row.get('smoking'), 0))
        smoking_intensity = self._safe_number(row.get('smoking_intensity'), 0) or 0
        vaping = bool(self._safe_number(row.get('vaping'), 0))
        alcohol = self._safe_number(row.get('alcohol'), 0) or 0
        caffeine = self._safe_number(row.get('caffeine_intake'), 0) or 0

        if smoking:
            score -= 15
            risks.append("Smoking is a major cardiovascular risk driver.")
            actions.append("Initiate a structured smoking cessation plan within the next 2 weeks.")
        elif smoking_intensity > 0:
            score -= 8
            actions.append("Reduce cigarette or bidi use progressively; seek cessation support.")
        else:
            positives.append("Smoke-free lifestyle supports heart and lung health.")

        if vaping:
            score -= 5
            actions.append("Limit or stop vaping; assess for nicotine dependence.")

        if alcohol >= 2:
            score -= 6
            actions.append("Cap alcohol consumption to special occasions at ≤2 drinks/week.")
        elif alcohol == 0:
            positives.append("Alcohol avoidance protects liver and cardiovascular health.")

        if caffeine >= 3:
            score -= 3
            actions.append("Keep caffeine below 2 cups daily to stabilize sleep and blood pressure.")

        score = float(np.clip(score, 5.0, 100.0))
        return {
            'label': 'Substance Exposure',
            'score': round(score, 1),
            'status': self._score_status(score),
            'risks': self._unique_messages(risks),
            'positives': self._unique_messages(positives),
            'actions': self._unique_messages(actions)
        }

    def _score_medication(self, row):
        score = 78.0
        risks = []
        positives = []
        actions = []

        adherence = self._safe_number(row.get('medication_adherence'), None)
        side_effects = bool(self._safe_number(row.get('medication_side_effects'), 0))

        if adherence is not None:
            if adherence <= 1:
                score -= 12
                risks.append("Medication adherence concerns flagged.")
                actions.append("Set reminders or pill organizers to improve adherence consistency.")
            elif adherence >= 3:
                score += 4
                positives.append("Strong medication adherence reported.")

        if side_effects:
            score -= 4
            actions.append("Discuss side effects with your clinician for adjustments.")

        score = float(np.clip(score, 5.0, 100.0))
        return {
            'label': 'Medication & Treatment Adherence',
            'score': round(score, 1),
            'status': self._score_status(score),
            'risks': self._unique_messages(risks),
            'positives': self._unique_messages(positives),
            'actions': self._unique_messages(actions)
        }

    def _score_preventive(self, row):
        score = 70.0
        risks = []
        positives = []
        actions = []

        checkup_months = self._safe_number(row.get('last_checkup_months'), None)
        vaccination_status = self._safe_number(row.get('vaccination_status'), None)

        if checkup_months is not None:
            if checkup_months > 18:
                score -= 10
                actions.append("Schedule a comprehensive health check-up soon (last visit >18 months).")
            elif checkup_months <= 12:
                score += 4
                positives.append("Routine medical check-ups maintained.")

        if vaccination_status is not None:
            if vaccination_status <= 1:
                score -= 6
                actions.append("Review vaccination status with healthcare provider (some doses overdue).")
            elif vaccination_status >= 2:
                score += 3

        score = float(np.clip(score, 5.0, 100.0))
        return {
            'label': 'Preventive Care & Screenings',
            'score': round(score, 1),
            'status': self._score_status(score),
            'risks': self._unique_messages(risks),
            'positives': self._unique_messages(positives),
            'actions': self._unique_messages(actions)
        }

    def _score_environment(self, row):
        score = 72.0
        risks = []
        positives = []
        actions = []

        exposure = self._safe_number(row.get('environmental_exposure'), None)
        shift_work = bool(self._safe_number(row.get('shift_work'), 0))

        if exposure is not None:
            if exposure >= 2:
                score -= 8
                actions.append("Use protective measures against pollution or chemical exposure.")
            elif exposure == 0:
                positives.append("Minimal environmental hazard exposure reported.")

        if shift_work:
            score -= 6
            actions.append("Stabilize sleep schedule despite shift work; prioritize recovery days.")

        score = float(np.clip(score, 5.0, 100.0))
        return {
            'label': 'Environment & Occupational Factors',
            'score': round(score, 1),
            'status': self._score_status(score),
            'risks': self._unique_messages(risks),
            'positives': self._unique_messages(positives),
            'actions': self._unique_messages(actions)
        }

    def _unique_messages(self, messages):
        seen = set()
        unique = []
        for msg in messages:
            if not msg:
                continue
            if msg not in seen:
                unique.append(msg)
                seen.add(msg)
        return unique

    def _clamp_percentage(self, value):
        return max(0.0, min(100.0, value))

    def _clamp_adjustment(self, value):
        return max(-20.0, min(20.0, value))


advanced_predictor = AdvancedRiskPredictor()

