export interface TreatmentCost {
  name: string;
  government: { min: number; max: number; avg: number };
  semiPrivate: { min: number; max: number; avg: number };
  private: { min: number; max: number; avg: number };
  factors: Array<{ name: string; impact: string }>;
}

export const treatmentCostsDatabase: Record<string, TreatmentCost> = {
  "appendectomy": {
    name: "Appendectomy (Appendix Removal)",
    government: { min: 15000, max: 35000, avg: 25000 },
    semiPrivate: { min: 40000, max: 80000, avg: 60000 },
    private: { min: 80000, max: 150000, avg: 110000 },
    factors: [
      { name: "Surgery Type", impact: "Laparoscopic surgery costs 20-30% more than open surgery but has faster recovery" },
      { name: "Hospital Location", impact: "Metro cities typically charge 30-50% more than tier-2/3 cities" },
      { name: "Surgeon Experience", impact: "Senior surgeons may charge 15-25% more" },
      { name: "Complications", impact: "Perforated appendix or infections can increase costs by 40-60%" },
      { name: "Hospital Stay", impact: "Each additional day adds ₹2,000-₹8,000 depending on hospital type" },
    ],
  },
  "cataract-surgery": {
    name: "Cataract Surgery (Per Eye)",
    government: { min: 5000, max: 15000, avg: 10000 },
    semiPrivate: { min: 20000, max: 40000, avg: 30000 },
    private: { min: 40000, max: 100000, avg: 65000 },
    factors: [
      { name: "Lens Type", impact: "Premium multifocal lenses cost ₹20,000-₹60,000 more than standard lenses" },
      { name: "Technology Used", impact: "Phacoemulsification or laser-assisted surgery is more expensive but safer" },
      { name: "Surgeon Expertise", impact: "Experienced ophthalmologists may charge premium fees" },
      { name: "Post-operative Care", impact: "Follow-up visits and medications add ₹3,000-₹8,000" },
    ],
  },
  "cesarean-section": {
    name: "Cesarean Section (C-Section)",
    government: { min: 20000, max: 50000, avg: 35000 },
    semiPrivate: { min: 50000, max: 100000, avg: 75000 },
    private: { min: 100000, max: 250000, avg: 150000 },
    factors: [
      { name: "Emergency vs Planned", impact: "Emergency C-sections can cost 20-30% more" },
      { name: "Anesthesia Type", impact: "Spinal vs general anesthesia affects overall cost" },
      { name: "NICU Requirements", impact: "If baby needs NICU care, costs can increase by ₹50,000-₹200,000" },
      { name: "Room Type", impact: "Private room adds ₹2,000-₹10,000 per day" },
      { name: "Duration of Stay", impact: "Normal stay is 3-5 days; complications extend this" },
    ],
  },
  "knee-replacement": {
    name: "Total Knee Replacement",
    government: { min: 80000, max: 150000, avg: 115000 },
    semiPrivate: { min: 150000, max: 300000, avg: 225000 },
    private: { min: 250000, max: 500000, avg: 350000 },
    factors: [
      { name: "Implant Quality", impact: "Imported implants cost ₹50,000-₹100,000 more than Indian brands" },
      { name: "Unilateral vs Bilateral", impact: "Both knees cost nearly double but slightly discounted" },
      { name: "Hospital Stay", impact: "Typically 5-7 days; ICU needs add significant cost" },
      { name: "Physiotherapy", impact: "Post-surgery rehabilitation for 2-3 months adds ₹15,000-₹40,000" },
      { name: "Surgeon Experience", impact: "Orthopedic specialists with high success rates charge premium" },
    ],
  },
  "heart-bypass-surgery": {
    name: "Coronary Artery Bypass Surgery (CABG)",
    government: { min: 150000, max: 300000, avg: 225000 },
    semiPrivate: { min: 300000, max: 500000, avg: 400000 },
    private: { min: 500000, max: 1000000, avg: 700000 },
    factors: [
      { name: "Number of Grafts", impact: "More grafts (vessels bypassed) increase cost by ₹30,000-₹50,000 each" },
      { name: "ICU Duration", impact: "Typically 3-5 days in ICU; each day costs ₹10,000-₹30,000" },
      { name: "Complications", impact: "Infections or additional procedures can double total cost" },
      { name: "Cardiac Surgeon Expertise", impact: "Top cardiac surgeons charge 30-50% premium" },
      { name: "Pre and Post Care", impact: "Medications, tests, and follow-ups add ₹30,000-₹80,000" },
    ],
  },
  "hernia-repair": {
    name: "Hernia Repair Surgery",
    government: { min: 20000, max: 40000, avg: 30000 },
    semiPrivate: { min: 40000, max: 80000, avg: 60000 },
    private: { min: 70000, max: 150000, avg: 100000 },
    factors: [
      { name: "Hernia Type", impact: "Inguinal, umbilical, or hiatal hernias have different complexities" },
      { name: "Mesh Quality", impact: "Premium mesh materials cost ₹10,000-₹30,000 more" },
      { name: "Laparoscopic vs Open", impact: "Laparoscopic surgery costs 25-40% more but faster recovery" },
      { name: "Recurrence", impact: "Repeat surgery for recurring hernia may cost 20% more" },
    ],
  },
  "gallbladder-removal": {
    name: "Cholecystectomy (Gallbladder Removal)",
    government: { min: 25000, max: 50000, avg: 37500 },
    semiPrivate: { min: 50000, max: 90000, avg: 70000 },
    private: { min: 80000, max: 180000, avg: 120000 },
    factors: [
      { name: "Emergency vs Elective", impact: "Emergency surgery for acute cholecystitis costs 20-30% more" },
      { name: "Surgical Approach", impact: "Laparoscopic (keyhole) is preferred; open surgery for complications" },
      { name: "Hospital Stay", impact: "Usually 1-3 days; complications extend stay" },
      { name: "Complications", impact: "Bile duct injury or infections significantly increase costs" },
    ],
  },
  "normal-delivery": {
    name: "Normal Vaginal Delivery",
    government: { min: 10000, max: 25000, avg: 17500 },
    semiPrivate: { min: 25000, max: 60000, avg: 42500 },
    private: { min: 50000, max: 150000, avg: 90000 },
    factors: [
      { name: "Epidural Anesthesia", impact: "Pain management adds ₹8,000-₹20,000" },
      { name: "Complications", impact: "Assisted delivery (forceps/vacuum) or hemorrhage increases costs" },
      { name: "NICU Needs", impact: "Baby requiring special care adds ₹30,000-₹150,000" },
      { name: "Room Type", impact: "Private room vs general ward adds ₹1,500-₹8,000 per day" },
      { name: "Duration of Stay", impact: "Normal is 2-3 days; complications extend this" },
    ],
  },
  "dental-implant": {
    name: "Dental Implant (Per Tooth)",
    government: { min: 15000, max: 30000, avg: 22500 },
    semiPrivate: { min: 25000, max: 50000, avg: 37500 },
    private: { min: 40000, max: 100000, avg: 65000 },
    factors: [
      { name: "Implant Brand", impact: "European/American brands cost more than Korean/Indian brands" },
      { name: "Bone Grafting", impact: "If bone density is low, grafting adds ₹15,000-₹40,000" },
      { name: "Crown Material", impact: "Zirconia crowns cost more than porcelain-fused-to-metal" },
      { name: "Dentist Experience", impact: "Specialists in implantology charge premium rates" },
      { name: "Number of Implants", impact: "Multiple implants often get slight bulk discount" },
    ],
  },
  "diabetes-management-annual": {
    name: "Diabetes Management (Annual Cost)",
    government: { min: 12000, max: 30000, avg: 21000 },
    semiPrivate: { min: 30000, max: 60000, avg: 45000 },
    private: { min: 50000, max: 120000, avg: 80000 },
    factors: [
      { name: "Medication Type", impact: "Insulin therapy costs more than oral medications" },
      { name: "Testing Frequency", impact: "Daily glucose monitoring adds ₹8,000-₹20,000 annually" },
      { name: "Complications", impact: "Managing neuropathy, retinopathy, or kidney issues increases costs significantly" },
      { name: "Consultation Frequency", impact: "Quarterly specialist visits vs annual check-ups" },
      { name: "Lifestyle Programs", impact: "Dietician and fitness coaching add ₹15,000-₹40,000" },
    ],
  },
};

export const treatmentsList = Object.keys(treatmentCostsDatabase).map(
  (key) => treatmentCostsDatabase[key].name
);
