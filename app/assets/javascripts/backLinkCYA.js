document.addEventListener('DOMContentLoaded', function() {
              console.log('JavaScript is enabled');
              const backLink = document.getElementById('back-link');
              backLink.setAttribute('href', backLink.getAttribute('href').replace('&isJsEnabled=false', '') + '&isJsEnabled=true');
              const changeLinkForUpload = document.getElementById('upload-row');
              changeLinkForUpload.setAttribute('href', changeLinkForUpload.getAttribute('href').replace('isJsEnabled=false', '') + 'isJsEnabled=true');
            });