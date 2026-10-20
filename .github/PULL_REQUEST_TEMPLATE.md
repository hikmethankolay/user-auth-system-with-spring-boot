## What and why

<!-- What does this change, and why is it needed? Link the issue: "Closes #123". -->

## How it was tested

<!-- Tests added or updated, manual checks. -->

## Checklist

- [ ] `scripts/test.sh` passes
- [ ] `scripts/docs.sh --strict` passes
- [ ] New configuration is in `.env.example` and the Configuration guide
- [ ] API changes: `api/openapi.json` regenerated (`-Dopenapi.update=true`) and the diff reviewed
- [ ] Database changes are a new Flyway migration
