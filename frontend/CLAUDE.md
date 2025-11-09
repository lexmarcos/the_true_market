## Project Overview

This is a CS2 skin price comparison platform. The application monitors skin prices across various third-party marketplaces and compares them with Steam Community Market prices to identify profitable trading opportunities.

**Tech Stack:**

- Next.js 15
- TypeScript
- Shadcn UI
- Tailwind CSS

**Backend API:** FastAPI available at `http://localhost:8000/docs`

---

## Environment Variables

**Always use environment variables for configuration:**

- `NEXT_PUBLIC_API_URL`: Backend API URL (default: `http://localhost:8000`)
- `NEXT_PUBLIC_SITE_NAME`: Site name displayed throughout the application (default: `The True Market`)

**Important:** When referencing the site name in code, ALWAYS use `process.env.NEXT_PUBLIC_SITE_NAME` instead of hardcoding "The True Market" or any other name.

Example:

```typescript
const SITE_NAME = process.env.NEXT_PUBLIC_SITE_NAME || "The True Market";
```

---

## Architecture: MVVM Pattern

The project follows a strict MVVM (Model-View-ViewModel) architecture:

### Model (`*.model.ts`)

- Custom hooks responsible for ALL business logic
- State management
- HTTP communication
- Data processing
- Variable declarations and computations
- **Location:** Handles the "what" and "how" of data

### View (`*.view.tsx`)

- **STRICTLY** presentation layer (JSX only)
- **🚫 ABSOLUTELY NO LOGIC ALLOWED**
- **🚫 ABSOLUTELY NO VARIABLE DECLARATIONS OR COMPUTATIONS**
- **🚫 NO `const`, `let`, `var` declarations**
- **🚫 NO conditional logic, ternary operators, or calculations**
- **🚫 NO array methods (`.map()`, `.filter()`, `.reduce()`, etc.)**
- **🚫 NO object destructuring or transformations**
- Receives functions via props for event handlers
- Receives pre-computed values via props
- Only concerns: rendering JSX using props directly
- **Location:** Handles the "look" ONLY

**❌ FORBIDDEN in View:**

```typescript
// ❌ NO variable declarations
const profitableSkins = skins.filter(s => s.profitMargin > 10);
const totalProfit = steamPrice - marketPrice;
const profitPercentage = ((steamPrice - marketPrice) / marketPrice * 100).toFixed(2);

// ❌ NO logic in JSX
{skins.filter(s => s.profitMargin > 0).map(skin => ...)}
{profitMargin > 15 ? "High Profit" : "Low Profit"}

// ❌ NO computations
{(steamPrice * 0.87).toFixed(2)} // Steam fee calculation
```

**✅ CORRECT in View:**

```typescript
// ✅ Only receive and render
export function SkinListView({
  profitableSkins,
  totalProfit,
  profitPercentage,
}: SkinListViewProps) {
  return (
    <div>
      <h1>Total Profit: ${totalProfit}</h1>
      <p>Margin: {profitPercentage}%</p>
      {profitableSkins.map((skin) => (
        <SkinCard key={skin.id} {...skin} />
      ))}
    </div>
  );
}
```

### ViewModel (`page.tsx` or `index.tsx`)

- Orchestrates Model and View
- Instantiates the Model hook
- Passes Model properties to View
- Receives and forwards props to Model when needed
- **Location:** Handles the "connection"

### Supporting Files

- **schemas** (`*.schemas.ts`): Zod validation schemas
- **types** (`*.types.ts`): TypeScript type definitions
- **mutations** (`*.mutations.ts`): POST request mutation logic
- **tests** (`*.test.ts`): Unit and integration tests
- **loading** (`*.loading.tsx`): Skeleton loading states (pages only)

---

## File Naming Convention

Format: `name-of-file.[layer/type].(ts|tsx)`

### Page Example (`/skins`)
