# Business Rules - Tech Challenge Fase 04

# Serverless Functions

## Function: Receive Feedback
- Trigger: HTTP (POST /avaliacao)
- Responsibility: Receive, validate, and store feedback
- Actions: Persist to database, trigger notification if urgent

## Function: Urgency Notification
- Trigger: Event (urgent feedback)
- Responsibility: Send email/alert to administrators
- Actions: Compose message, send via notification service

## Function: Weekly Report
- Trigger: Schedule (e.g., weekly cron)
- Responsibility: Generate consolidated feedback report
- Actions: Query database, calculate averages, send report