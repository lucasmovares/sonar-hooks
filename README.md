# SonarQube Issue Hooks Plugin

[![Build and Test](https://github.com/movares/sonar-hooks/actions/workflows/build.yml/badge.svg)](https://github.com/movares/sonar-hooks/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A SonarQube plugin that sends HTTP webhooks when issues are updated during analysis. Perfect for integrating SonarQube with external systems, notifications, or workflow automation.

## Features

- **Real-time Webhooks**: Automatically sends HTTP POST requests when issues are created, updated, or resolved
- **Secure Communication**: Optional HMAC-SHA256 signature support for webhook authentication
- **Configurable**: Flexible settings for webhook URL, timeout, retry logic, and more
- **Reliable**: Built-in retry mechanism with exponential backoff for failed webhook deliveries
- **Comprehensive Payload**: Rich JSON payload with detailed issue and project information

## Installation

### Latest Release

Download the latest release from the [releases page](https://github.com/movares/sonar-hooks/releases).

1. **Download the plugin JAR** from the latest release assets
2. **Copy to SonarQube**: Place the JAR file in the `extensions/plugins/` directory of your SonarQube instance
3. **Restart SonarQube**: Restart your SonarQube server
4. **Configure**: Set up webhook settings in the SonarQube administration panel

### Manual Build

To build the plugin from source:

```bash
git clone https://github.com/movares/sonar-hooks.git
cd sonar-hooks/src/sonar-hooks
mvn clean package
```

The built plugin will be available at `target/sonar-hooks-plugin-*.jar`.

## Configuration

Configure the plugin in SonarQube's administration panel under **General > Issue Hooks**:

| Setting | Description | Default |
|---------|-------------|---------|
| Enable Issue Webhooks | Enable/disable webhook notifications | `false` |
| Webhook URL | HTTP endpoint to receive webhook notifications | - |
| Webhook Secret | Optional secret for HMAC-SHA256 authentication | - |
| Webhook Timeout (ms) | HTTP request timeout in milliseconds | `10000` |
| Webhook Retry Count | Number of retry attempts for failed requests | `3` |

## Webhook Payload

The plugin sends a JSON payload with comprehensive issue information:

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
    "resolution": null,
    "assignee": null,
    "author": "john.doe",
    "component": "src/main/java/com/example/MyClass.java",
    "rule": "java:S1481",
    "message": "Remove this unused variable",
    "line": 42,
    "effort": "5min",
    "creationDate": "2025-01-16T12:34:56.789Z",
    "updateDate": "2025-01-16T12:34:56.789Z",
    "closeDate": null
  }
}
```

## Security

### HMAC Signature Verification

When a webhook secret is configured, the plugin adds an `X-Hub-Signature-256` header with an HMAC-SHA256 signature:

```
X-Hub-Signature-256: sha256=<signature>
```

To verify the signature in your webhook receiver:

```python
import hmac
import hashlib

def verify_signature(payload, signature, secret):
    expected = 'sha256=' + hmac.new(
        secret.encode('utf-8'),
        payload.encode('utf-8'),
        hashlib.sha256
    ).hexdigest()
    return hmac.compare_digest(expected, signature)
```

## Usage Examples

### Basic Webhook Receiver (Node.js)

```javascript
const express = require('express');
const app = express();

app.use(express.json());

app.post('/webhooks/sonarqube', (req, res) => {
    const { event, action, project, issue } = req.body;
    
    console.log(`Received ${event} (${action}) for project ${project.name}`);
    console.log(`Issue: ${issue.message} at ${issue.component}:${issue.line}`);
    
    // Process the webhook data here
    
    res.status(200).send('OK');
});

app.listen(3000, () => {
    console.log('Webhook server listening on port 3000');
});
```

### Slack Integration Example

```bash
# Configure webhook URL to post to Slack
curl -X POST "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "New SonarQube issue: '"${issue.message}"' in '"${project.name}"'"
  }'
```

## Compatibility

| SonarQube Version | Plugin Version |
|-------------------|----------------|
| 9.4+              | 1.0.0+         |

## Support

This plugin is developed and maintained by [Movares](https://www.movares.com). For support:

- **Issues**: Report bugs or request features via [GitHub Issues](https://github.com/movares/sonar-hooks/issues)
- **Discussions**: Join the conversation in [GitHub Discussions](https://github.com/movares/sonar-hooks/discussions)
- **Documentation**: Check the [Wiki](https://github.com/movares/sonar-hooks/wiki) for detailed guides

## Development

### Prerequisites

- Java 11 or higher
- Apache Maven 3.6+
- SonarQube 9.4+ for testing

### Building

```bash
mvn clean compile
```

### Testing

```bash
mvn test
```

### Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Changelog

See [RELEASES.md](RELEASES.md) for detailed release notes and changelog.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

---

**Note**: This plugin is not officially maintained by SonarSource. It is an independent project developed to extend SonarQube's webhook capabilities. 