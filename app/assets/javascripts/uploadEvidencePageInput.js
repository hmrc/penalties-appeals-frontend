document.addEventListener('DOMContentLoaded', function() {
      console.log('JavaScript is enabled');
      const form = document.getElementById('upload-evidence-question-form');
      const input = document.createElement('input');
      input.setAttribute('type', 'hidden');
      input.setAttribute('name', 'isJsEnabled');
      input.setAttribute('value', 'true');
      form.appendChild(input);
    });