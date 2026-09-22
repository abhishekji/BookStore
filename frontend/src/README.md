# Frontend source organization

Production code and its unit test are colocated in the same feature folder:

```text
src/
├── api/client/
│   ├── client.ts
│   └── client.test.ts
├── application/auth-state/
│   ├── AuthState.tsx
│   └── AuthState.test.tsx
├── components/book-card/
│   ├── BookCard.tsx
│   └── BookCard.test.tsx
├── domain/types/
│   └── types.ts
└── pages/catalogue/
    ├── CataloguePage.tsx
    └── CataloguePage.test.tsx
```

`main.tsx`, styling, Vite type declarations, and test setup are application/tooling entrypoints rather than independently testable features.
