# SMART Health Guide+ Design Guidelines

## Design Approach

**Selected Approach:** Hybrid - Modern Health Platform Reference + Material Design System

**References:** Modern health platforms like Healthline, Mayo Clinic patient portals, and wellness apps that balance professionalism with approachability.

**Core Principles:**
- Medical credibility through clean, organized layouts
- Educational accessibility with clear information hierarchy
- Trust-building through consistent, professional design
- Approachable interface that reduces medical intimidation

---

## Typography System

**Font Families:**
- Primary (UI/Body): Inter or "system-ui" - clean, highly readable
- Headings: Inter SemiBold/Bold - professional hierarchy
- Data/Numbers: "SF Mono" or monospace - for medical values and results

**Type Scale:**
- Hero/Page Titles: text-4xl md:text-5xl font-bold
- Section Headings: text-2xl md:text-3xl font-semibold
- Module Cards: text-xl font-semibold
- Body Text: text-base leading-relaxed
- Captions/Labels: text-sm
- Disclaimers: text-xs md:text-sm italic
- Data Values: text-lg md:text-xl font-mono font-semibold

**Line Heights:**
- Headings: leading-tight
- Body: leading-relaxed
- Disclaimers: leading-normal

---

## Layout System

**Spacing Primitives:**
Primary units: 2, 4, 6, 8, 12, 16, 20, 24 (Tailwind scale)

**Common Patterns:**
- Component padding: p-6 md:p-8
- Section spacing: py-16 md:py-24
- Card gaps: gap-6 md:gap-8
- Form fields: space-y-4
- Button spacing: px-6 py-3

**Container Strategy:**
- Max width: max-w-7xl mx-auto
- Content sections: max-w-4xl mx-auto for forms/content
- Dashboard cards: grid with max-w-6xl
- Full-width sections with inner containers for visual impact

**Grid Systems:**
- Dashboard modules: grid-cols-1 md:grid-cols-2 lg:grid-cols-3
- Symptom checker: Two-column layout (selection + results) on desktop
- Report analyzer: Single column with max-w-3xl for optimal readability

---

## Core Components

### Navigation Header
- Fixed top navigation with backdrop blur
- Logo/branding on left
- Horizontal navigation links for main modules (desktop)
- Mobile: Hamburger menu with full-screen overlay
- Prominent medical disclaimer badge/icon in header
- Height: h-16 md:h-20

### Dashboard/Homepage
**Hero Section:**
- Clean, centered content with max-w-4xl
- Large heading explaining system purpose
- Subheading with educational disclaimer
- Two-column grid on tablet+ for key statistics/trust indicators
- Height: min-h-[60vh] with vertical centering
- Background: Subtle medical pattern or abstract health imagery

**Module Cards Grid:**
- 3-column grid on desktop, 2 on tablet, 1 on mobile
- Each card: rounded-xl with border, p-6 md:p-8
- Icon at top (4rem size), title, description, arrow/button
- Hover: Subtle lift effect (translate-y-1)
- Consistent card height with flex layout

### Symptom Checker Interface
**Layout:**
- Two-column split on desktop (60/40)
- Left: Symptom selection interface with search + checkboxes
- Right: Results panel (sticky on scroll)
- Mobile: Stacked with results appearing below selection

**Symptom Selection:**
- Search bar: w-full with icon, rounded-lg, p-4
- Categorized symptom groups with expandable sections
- Checkbox grid: grid-cols-2 md:grid-cols-3 gap-4
- Selected symptoms: Chip display above results with remove option

**Results Display:**
- Card-based layout with severity indicator
- Top conditions with percentage bars
- Expandable details for each condition
- Clear "Educational Only" disclaimer banner at top

### Health Chatbot
**Layout:**
- Centered chat container: max-w-3xl
- Chat history: min-h-[500px] max-h-[600px] with scroll
- Fixed input at bottom with sticky positioning

**Message Design:**
- User messages: Align right, rounded-l-2xl rounded-tr-2xl, max-w-[80%]
- Bot messages: Align left, rounded-r-2xl rounded-tl-2xl, max-w-[80%]
- Timestamp: text-xs below each message
- Disclaimer banner above chat input (persistent)

**Input Area:**
- Textarea with auto-expand (max 4 lines)
- Send button integrated on right
- Character counter if needed
- Suggested questions as chips above input

### Test Information Lookup
**Layout:**
- Search/filter bar at top: w-full max-w-2xl mx-auto
- Condition selector: Dropdown or autocomplete
- Results: Two-column cards (Recommended vs. Avoid)

**Test Cards:**
- Grid layout within each section
- Icon, test name, brief description
- "Why?" expandable section
- Cost range indicator if available

### Cost Estimation Tool
**Form Layout:**
- Single column form: max-w-2xl mx-auto
- Grouped sections: Treatment type, Hospital category, Location
- Large dropdown selectors with search
- Results panel: Card format with breakdown

**Results Display:**
- Primary cost range: Large, prominent text-3xl
- Breakdown table: Factor-by-factor cost components
- Disclaimer about estimates
- Comparison view if multiple options selected

### Report Analyzer
**Upload Section:**
- Large dropzone: min-h-[200px] with dashed border
- File upload or manual entry toggle
- Parameter input: Two-column grid of labeled inputs
- Each input: Label, input field, unit indicator

**Results Display:**
- Table format with parameter, value, normal range, status
- Visual indicators: Icon + subtle background for out-of-range values
- Summary card at top with overall assessment
- Detailed explanations in expandable sections below table

### Footer
**Structure:**
- Three-column layout (desktop), stacked (mobile)
- Column 1: About, mission statement, logo
- Column 2: Quick links to all modules
- Column 3: Disclaimer, resources, contact
- Bottom bar: Copyright, terms, privacy links
- Padding: py-12 md:py-16

---

## Disclaimer System

**Primary Disclaimer Banner:**
- Appears on every module page
- Positioned at top of content area (below header)
- Prominent border, icon, clear text
- Padding: p-4 md:p-6
- Dismissible with "Understood" but persists per session

**Inline Disclaimers:**
- Results sections: Small disclaimer text above results
- Chatbot: Persistent banner above input
- Cost estimates: Inline with results

---

## Form Elements

**Input Fields:**
- Consistent styling: rounded-lg, border-2, p-3
- Labels: font-medium, mb-2
- Error states: Red border, error text below
- Helper text: text-sm below input

**Buttons:**
- Primary action: Large, rounded-lg, px-8 py-3, font-semibold
- Secondary: Outlined version with same sizing
- Icon buttons: Square, p-3, rounded-lg
- Disabled state: Reduced opacity, cursor-not-allowed

**Dropdowns/Selects:**
- Match input field styling
- Chevron icon on right
- Option padding: p-3

---

## Data Visualization

**Progress/Severity Bars:**
- Full width with rounded ends
- Height: h-2 md:h-3
- Background track visible
- Percentage label on right

**Status Indicators:**
- Badge format: rounded-full, px-3 py-1, text-xs font-semibold
- Icons with labels for medical values (normal/high/low)

**Tables:**
- Striped rows for readability
- Sticky header on scroll
- Responsive: Card view on mobile
- Cell padding: px-4 py-3

---

## Images

**Hero Section Image:**
- Abstract medical/health visualization
- Doctors collaborating, medical technology, or wellness imagery
- Placement: Background with overlay OR side-by-side with content
- Size: Full width, min-h-[60vh]

**Module Card Icons:**
- Medical-themed iconography
- Size: 64x64px, consistent style
- Placement: Top of each dashboard card

**About/Trust Section:**
- Professional medical imagery showing technology + care
- Team or technology workspace photos
- Placement: Full-width section between modules, py-20

---

## Responsive Breakpoints

- Mobile: base (< 768px) - Single column, stacked navigation
- Tablet: md (768px+) - Two columns where appropriate
- Desktop: lg (1024px+) - Full multi-column layouts
- Wide: xl (1280px+) - Max container widths apply

---

## Accessibility Requirements

- All interactive elements: Keyboard navigable, focus visible
- Form labels: Properly associated with inputs
- ARIA labels for icon buttons
- Color contrast: Meets WCAG AA standards minimum
- Semantic HTML throughout (nav, main, section, article)
- Skip to content link in header