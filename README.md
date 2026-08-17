# assessment-service

Assessment domain, extracted from the `prevaia/back` monolith (Semaine 4, Jour 4 matin).
Owns `assessment`, `assessment_section_score`, `ai_summary`, `assessment_values`,
`assessment_attachments`, `ai_response`, `ref_assessment_status`, plus local read
models `supplier_projection`/`project_projection` (synced from Partner Service via
RabbitMQ) and a read-only mirror of Catalog's `ref_sections`/`ref_questions`/
`ref_question_options`/`ref_score_range`/`ref_attachments` (see
`back/assessment-service-extraction-migration.sql` for how that mirror is populated
and why).

Runs on port `8085`, own local Postgres (`assessment_local`, port `5437`).

Not yet routed through the Gateway — `back/assessment` still serves production
traffic. See `back/saga-assessment-service.md` and the Semaine 4 programme doc for
the cutover plan (Jour 4 après-midi).
