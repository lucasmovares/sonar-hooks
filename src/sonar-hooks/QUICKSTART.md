# Quick Start Guide

## 🧪 Test Your Setup

### 1. Create a Test Webhook Endpoint

```javascript
// Simple Express.js webhook receiver
const express = require('express');
const app = express();

app.use(express.json());

app.post('/webhooks/sonarqube', (req, res) => {
  console.log('Received SonarQube webhook:', JSON.stringify(req.body, null, 2));
  res.status(200).send('OK');
});

app.listen(3000, () => {
  console.log('Webhook server listening on port 3000');
});
```

### 2. Run a SonarQube Analysis

Trigger an analysis on one of your projects to test the webhook functionality.

## 📋 What You'll Receive

```json
{
  "timestamp": "2025-01-16T12:34:56.789Z",
  "event": "issue_updated",
  "action": "created",
  "project": {
    "key": "my-project",
    "name": "My Project"
  },
  "issue": {
    "key": "AXyZ123456",
    "type": "BUG",
    "severity": "MAJOR",
    "status": "OPEN",
    "message": "Remove this unused variable",
    "component": "src/main/java/com/example/MyClass.java",
    "line": 42
  }
}
```

## 🔧 Troubleshooting

### Plugin Not Loading?
```bash
# Check SonarQube logs
kubectl logs -n sonarqube deployment/sonarqube-sonarqube | grep -i "hooks"
```

### Webhooks Not Sending?
1. Check plugin is enabled in SonarQube settings
2. Verify webhook URL is accessible
3. Check SonarQube logs for errors
