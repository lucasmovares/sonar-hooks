window.registerExtension('sonar-hooks/admin', function(options) {
  var container = options.el;
  
  function loadConfig() {
    window.SonarRequest.getJSON('/api/settings/values', {
      keys: [
        'sonar.hooks.webhook.enabled',
        'sonar.hooks.webhook.url',
        'sonar.hooks.webhook.timeout',
        'sonar.hooks.webhook.retryCount'
      ].join(',')
    }).then(function(response) {
      // Update UI with current settings
      updateConfigDisplay(response.settings);
    });
  }

  function updateConfigDisplay(settings) {
    container.innerHTML = `
      <div class="page">
        <h1>Sonar Hooks Configuration</h1>
        <div class="settings-section">
          <h2>Webhook Settings</h2>
          <div class="settings-list">
            ${settings.map(setting => `
              <div class="settings-item">
                <label>${setting.key}</label>
                <div class="value">${setting.value || 'Not set'}</div>
              </div>
            `).join('')}
          </div>
        </div>
      </div>
    `;
  }

  // Load initial configuration
  loadConfig();

  // Return cleanup function
  return function() {
    container.innerHTML = '';
  };
}); 