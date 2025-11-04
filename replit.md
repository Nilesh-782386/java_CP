# SMART Health Guide+ - AI-Based Personal Medical Advisor System

## Overview

SMART Health Guide+ is an educational health information platform that provides AI-powered medical insights for informational purposes. The application offers five core modules: symptom-based disease prediction, health chatbot support, medical test recommendations, treatment cost estimation, and blood report analysis. This is a full-stack web application designed to help users learn about health conditions and make informed decisions, with clear disclaimers that it is not a substitute for professional medical advice.

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Frontend Architecture

**Framework & Build System:**
- React 18 with TypeScript for type-safe component development
- Vite as the build tool and development server
- Wouter for lightweight client-side routing (5 main routes: Home, Symptoms, Chat, Tests, Costs, Reports)

**UI Component System:**
- Shadcn/ui component library with Radix UI primitives for accessible, composable components
- Tailwind CSS for utility-first styling with custom design tokens
- "New York" style variant from Shadcn with custom theme configuration
- Design system based on modern health platforms (Healthline, Mayo Clinic) for medical credibility

**State Management:**
- TanStack Query (React Query) for server state management, data fetching, and caching
- Local component state with React hooks for UI interactions
- Toast notifications for user feedback

**Key Design Decisions:**
- Component-based architecture for reusability across all 5 health modules
- Responsive design with mobile-first breakpoints (lg: 1024px for desktop navigation)
- Accessibility-first approach using Radix UI primitives with ARIA attributes
- Educational focus with prominent disclaimer banners on all pages

### Backend Architecture

**Server Framework:**
- Express.js server with TypeScript
- REST API architecture using JSON for data exchange
- Middleware for request logging and JSON parsing

**Data Layer:**
- In-memory storage implementation (MemStorage class) for user management
- Static medical knowledge bases stored as TypeScript modules:
  - Symptoms database (200+ symptoms categorized by body system)
  - Diseases database (50+ conditions with severity ratings)
  - Chatbot knowledge base (FAQ-style responses)
  - Medical tests database (recommended/avoided tests per disease)
  - Treatment costs database (Indian hospital cost estimates)
  - Blood parameter ranges (normal/critical thresholds)

**API Endpoints:**
- GET `/api/symptoms` - Retrieve all symptoms
- GET `/api/diseases` - Retrieve all diseases
- GET `/api/treatments` - Retrieve treatment types
- POST `/api/check-symptoms` - Analyze symptoms and predict diseases
- POST `/api/chat` - Process health questions via chatbot
- GET `/api/tests/:diseaseId` - Get test recommendations for disease
- POST `/api/estimate-cost` - Estimate treatment costs
- POST `/api/analyze-report` - Analyze blood test parameters

**Algorithm Approach:**
- Symptom matching uses percentage-based similarity scoring
- Chatbot uses keyword matching against predefined knowledge base
- Test recommendations use rule-based JSON mappings
- Cost estimation uses static multi-tier pricing (government/semi-private/private hospitals)
- Report analysis compares values against normal/critical ranges

**Key Design Decisions:**
- RESTful API design for clear separation of concerns
- Static knowledge bases for fast, predictable responses without ML dependencies
- Validation using Zod schemas for type-safe request/response handling
- Educational focus: all responses include disclaimers and "when to seek help" guidance

### Database & ORM

**Database Configuration:**
- PostgreSQL as the target database (via Neon serverless driver)
- Drizzle ORM for type-safe database interactions
- Database URL configured via environment variable `DATABASE_URL`

**Schema Management:**
- Schema definitions in `shared/schema.ts` using Zod for validation
- Drizzle Kit for migrations (output to `./migrations` directory)
- Current implementation uses in-memory storage; database schema prepared for future persistence

**Migration Strategy:**
- `db:push` script to synchronize schema changes to database
- PostgreSQL dialect configuration in `drizzle.config.ts`

### Development & Build Process

**Development Environment:**
- Hot module replacement (HMR) via Vite
- TypeScript strict mode enabled across entire codebase
- Path aliases configured: `@/` for client, `@shared/` for shared types
- Replit-specific plugins for runtime error overlay and cartographer integration

**Build Process:**
- Client: Vite builds React app to `dist/public`
- Server: esbuild bundles Express server to `dist/index.js` (ESM format)
- Production build creates optimized bundle with external packages
- Incremental TypeScript compilation with build info caching

**Key Design Decisions:**
- Monorepo structure with shared types between client/server
- ESM modules throughout for modern JavaScript features
- Separate dev/build/start scripts for different environments
- Type checking separated from build process (`check` script)

## External Dependencies

### Core UI Framework
- **React & React DOM**: Frontend framework for component-based UI
- **Wouter**: Lightweight routing library (alternative to React Router)
- **TanStack Query**: Server state management and data synchronization

### UI Component Libraries
- **Radix UI**: Headless, accessible component primitives (20+ components: accordion, dialog, dropdown, select, etc.)
- **Shadcn/ui**: Pre-styled component library built on Radix UI
- **Lucide React**: Icon library for consistent iconography

### Styling & Design
- **Tailwind CSS**: Utility-first CSS framework
- **class-variance-authority**: Type-safe component variants
- **tailwind-merge & clsx**: Utility for merging Tailwind classes
- **cmdk**: Command palette component
- **embla-carousel-react**: Carousel/slider component

### Form Management
- **React Hook Form**: Form state and validation management
- **@hookform/resolvers**: Validation resolvers for React Hook Form
- **Zod**: Schema validation library (also used in Drizzle ORM)

### Backend & Database
- **Express**: Node.js web framework
- **Drizzle ORM**: Type-safe ORM for database operations
- **@neondatabase/serverless**: Serverless PostgreSQL driver
- **connect-pg-simple**: PostgreSQL session store for Express

### Development Tools
- **Vite**: Build tool and dev server
- **TypeScript**: Type-safe JavaScript
- **tsx**: TypeScript execution for Node.js
- **esbuild**: Fast JavaScript bundler for production builds
- **Drizzle Kit**: CLI for Drizzle ORM migrations

### Utilities
- **date-fns**: Date manipulation and formatting
- **nanoid**: Unique ID generation

### Replit Integration
- **@replit/vite-plugin-runtime-error-modal**: Development error overlay
- **@replit/vite-plugin-cartographer**: Code navigation tool
- **@replit/vite-plugin-dev-banner**: Development environment banner