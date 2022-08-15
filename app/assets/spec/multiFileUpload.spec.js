import {MultiFileUpload} from '../javascripts/upload/multiFileUpload';

function createFileList(files) {
    const dt = new DataTransfer();

    for (let i = 0, len = files.length; i < len; i++) {
        dt.items.add(files[i]);
    }

    return dt.files;
}

function getStatusResponse() {
    return {
        status: 'READY'
    };
}

function getStatusResponseWaiting() {
    return {
        status: 'WAITING'
    };
}

function getProvisionResponse() {
    return {
        reference: '123',
        uploadRequest: {
            fields: {},
            href: 'uploadUrl'
        }
    };
}

function getFailedResponse() {
    return {
        status: 'REJECTED',
        errorMessage: 'File 1 must be smaller than 6MB. Remove the file and try again.'
    };
}

describe('Multi File Upload component', () => {
    let instance;
    let container;
    let item;
    let item2;
    let input;

    describe('Given multi-file-upload component and its templates are present in DOM', () => {
        beforeEach(() => {
            document.body.insertAdjacentHTML('afterbegin', `
            <h1 id="hmrc-page-header">
                <span class="govuk-caption-l" id="penalty-information"></span>
            </h1>
            
        <form class="multi-file-upload"
          data-multi-file-upload-max-retries=1
          data-multi-file-upload-error-generic="File {fileNumber} could not be uploaded. Remove the file and try again."
          data-multi-file-upload-file-uploading="Uploading {fileNumber} {fileName}"
          data-multi-file-upload-file-uploaded="{fileNumber} {fileName} has been uploaded"
          data-multi-file-upload-file-removed="{fileNumber} {fileName} has been removed"
          data-multi-file-upload-remove-page-url-tpl="/remove-file/{fileRef}"
          data-multi-file-upload-error-prefix="Error:"
          >
          <ul class="multi-file-upload__item-list"></ul>

          <button type="button" class="multi-file-upload__add-another govuk-button govuk-button--secondary">Add another file</button>

          <p class="govuk-body multi-file-upload__form-status hidden" aria-hidden="true">
            Still transferring...
            <span class="file-upload__spinner ccms-loader"></span>
          </p>

          <input name="csrfToken" value="123"/>
          <div class="multi-file-upload__notifications govuk-visually-hidden" aria-live="polite" role="status"></div>

          <script type="text/x-template" id="multi-file-upload-item-tpl">
            <li class="multi-file-upload__item">
              <div class="govuk-form-group">
                <label class="govuk-label" for="file-{fileIndex}">File <span class="multi-file-upload__number">{fileNumber}</span></label>
                <div class="multi-file-upload__item-content">
                  <div class="multi-file-upload__file-container">
                    <input class="multi-file-upload__file govuk-file-upload" type="file" id="file-{fileIndex}">
                    <span class="multi-file-upload__file-name"></span>
                    <a class="multi-file-upload__file-preview"></a>
                  </div>

                  <div class="multi-file-upload__meta-container">
                    <div class="multi-file-upload__status">
                      <span class="multi-file-upload__progress">
                        <span class="multi-file-upload__progress-bar" role="status"></span>
                      </span>
                      <span class="multi-file-upload__tag govuk-tag">Uploaded</span>
                    </div>

                    <button type="button" class="multi-file-upload__remove-item govuk-link">
                      Remove
                      <span class="govuk-visually-hidden">File <span class="multi-file-upload__number">{fileNumber}</span><span class="multi-file-upload__file-name"></span></span>
                    </button>
                    <span class="multi-file-upload__removing">Removing...</span>
                  </div>
                </div>
              </div>
            </li>
          </script>

          <script type="text/x-template" id="error-manager-summary-tpl">
            <div class="govuk-error-summary">
              <ul class="govuk-list govuk-error-summary__list"></ul>
            </div>
          </script>

          <script type="text/x-template" id="error-manager-summary-item-tpl">
            <li>
              <a href="#{inputId}">{errorMessage}</a>
            </li>
          </script>

          <script type="text/x-template" id="error-manager-message-tpl">
            <span id="error-message-{fileNumber}" class="govuk-error-message">
              <span class="multi-file-upload__error-message">{errorMessage}</span>
            </span>
          </script>
        </form>
      `);

            container = document.querySelector('.multi-file-upload');
        });

        afterEach(() => {
            container.remove();
        });

        describe('And data-multi-file-upload-min-files is set to 1', () => {
            beforeEach(() => {
                container.dataset.multiFileUploadMinFiles = '1';
            });

            describe('When component is initialised', () => {
                beforeEach((done) => {
                    instance = new MultiFileUpload(container);
                    spyOn(instance, 'setDuplicateInsetText').and.returnValue(Promise.resolve({}));
                    spyOn(instance, 'requestProvisionUpload').and.callFake((file) => {
                        const response = getProvisionResponse();
                        const promise = Promise.resolve(response);

                        promise.then(() => {
                            instance.handleProvisionUploadCompleted(file, response);
                            done();
                        });

                        return promise;
                    });
                    instance.init();
                });

                it('Then one row should be present', (done) => {
                    expect(container.querySelectorAll('.multi-file-upload__item').length).toEqual(1);
                    done();
                });

                it('Then the remove button is not present', (done) => {
                    expect(container.querySelector('.multi-file-upload__remove-item').classList.contains('hidden')).toBeTrue();
                    done();
                });

                it('Then input should have data prop multiFileUploadFileRef="123"', (done) => {
                    input = container.querySelector('.multi-file-upload__file');
                    expect(input.dataset.multiFileUploadFileRef).toEqual('123');
                    done();
                });
            });

            describe('And data-multi-file-upload-max-files is set to 5', () => {
                beforeEach(() => {
                    container.dataset.multiFileUploadMaxFiles = '5';
                });

                describe('And component is initialised', () => {
                    beforeEach(() => {
                        instance = new MultiFileUpload(container);
                        spyOn(instance, 'setDuplicateInsetText').and.returnValue(Promise.resolve({}));
                        instance.init();
                    });

                    describe('When "Add another" button is clicked', () => {
                        beforeEach(() => {
                            spyOn(instance, 'requestProvisionUpload').and.returnValue(Promise.resolve(getProvisionResponse()));
                        });

                        it('Then 2 rows should be present', () => {
                            container.querySelector('.multi-file-upload__add-another').click();
                            expect(container.querySelectorAll('.multi-file-upload__item').length).toEqual(2);
                        });

                        it('Then "Add another" button should be hidden', () => {
                            container.querySelector('.multi-file-upload__add-another').click();
                            container.querySelector('.multi-file-upload__add-another').click();
                            container.querySelector('.multi-file-upload__add-another').click();
                            container.querySelector('.multi-file-upload__add-another').click();
                            const addAnotherBtn = container.querySelector('.multi-file-upload__add-another');

                            expect(addAnotherBtn.classList.contains('hidden')).toEqual(true);
                        });
                    });
                });
            });
        });

        describe('And component is initialised', () => {
            beforeEach((done) => {
                instance = new MultiFileUpload(container);
                const iseResponse = new Response(JSON.stringify({}), {status: 500, statusText: 'Something went wrong'});
                spyOn(window, 'fetch').and.resolveTo(iseResponse);
                spyOn(instance, 'setDuplicateInsetText').and.returnValue(Promise.resolve({}));
                spyOn(instance, 'redirectToErrorServicePage');
                instance.init();
                done();
            });

            it('Then should redirect to the service unavailable page when a 500 is returned', (done) => {
                expect(instance.redirectToErrorServicePage).toHaveBeenCalled();
                done();
            });
        });

        describe('And component is initialised', () => {
            beforeEach((done) => {
                instance = new MultiFileUpload(container);
                spyOn(instance, 'requestProvisionUpload').and.callFake((file) => {
                    const response = getProvisionResponse();
                    const promise = Promise.resolve(response);

                    promise.then(() => {
                        instance.handleProvisionUploadCompleted(file, response);
                        done();
                    });

                    return promise;
                });

                spyOn(instance, 'uploadFile').and.callThrough();
                spyOn(instance, 'setUploadingMessage').and.callThrough();
                spyOn(instance, 'requestUploadStatus').and.callFake((fileRef) => {
                    instance.handleRequestUploadStatusCompleted(fileRef, getStatusResponseWaiting());
                });
                spyOn(instance, 'setDuplicateInsetText').and.returnValue(Promise.resolve({}));
                instance.init();

                item = container.querySelector('.multi-file-upload__item');
                input = container.querySelector('.multi-file-upload__file');
                done();
            });

            describe('When user selects a file', () => {
                beforeEach(() => {

                    input.files = createFileList([new File([''], '/path/to/test.txt')]);
                    input.dispatchEvent(new Event('change'));

                });

                it('Then item should be in "uploading" state', (done) => {
                    expect(item.classList.contains('multi-file-upload__item--uploading')).toEqual(true);
                    done();
                });

                it('Then the progress bar should have the text "Uploading file [file number] [filename]"', (done) => {
                    expect(instance.setUploadingMessage).toHaveBeenCalled();
                    expect(item.querySelector('.multi-file-upload__progress-bar').textContent).toEqual('Uploading file 1 test.txt');
                    done();
                });

                it('Then uploadFile should have been called', (done) => {
                    expect(instance.uploadFile).toHaveBeenCalled();
                    done();
                });

                it('Then fileName should contain "test.txt"', (done) => {
                    const fileName = container.querySelector('.multi-file-upload__file-name');
                    expect(fileName.textContent).toEqual('test.txt');
                    done();
                });
            });
        });

        describe('And component is initialised', () => {
            beforeEach((done) => {
                instance = new MultiFileUpload(container);
                spyOn(instance, 'requestProvisionUpload').and.callFake((file) => {
                    const response = getProvisionResponse();
                    const promise = Promise.resolve(response);

                    promise.then(() => {
                        instance.handleProvisionUploadCompleted(file, response);
                        done();
                    });

                    return promise;
                });
                spyOn(instance, 'setDuplicateInsetText').and.returnValue(Promise.resolve({}));
                spyOn(instance, 'uploadFile').and.callFake((file) => {
                    instance.handleUploadFileCompleted(file.dataset.multiFileUploadFileRef);
                });
                spyOn(instance, 'requestUploadStatus').and.callFake((fileRef) => {
                    instance.handleRequestUploadStatusCompleted(fileRef, getFailedResponse());
                });
                spyOn(instance, 'delayedRequestUploadStatus').and.callFake((fileRef) => {
                    instance.requestUploadStatus(fileRef);
                });

                instance.init();
                item = container.querySelector('.multi-file-upload__item');
                input = container.querySelector('.multi-file-upload__file');
                done();
            });

            describe('When there is an error', () => {
                beforeEach((done) => {

                    input.files = createFileList([new File([''], '/path/to/test.txt')]);
                    input.dispatchEvent(new Event('change'));
                    done();
                });

                it('Then item should have the aria-describedby attribute', (done) => {
                    expect(input.hasAttribute('aria-describedby')).toEqual(true);
                    expect(input.getAttribute('aria-describedby')).toEqual('error-message-123');
                    done();
                });

                it('Then the document title should be prefixed with "Error:"', (done) => {
                    expect(document.title).toContain('Error:');
                    done();
                });

                 it('Then the correct error is shown', (done) => {
                    expect(item.querySelector('.multi-file-upload__error-message').textContent).toEqual('File 1 must be smaller than 6MB. Remove the file and try again.');
                    done();
                    });

                it('Then the remove button is not present', (done) => {
                    expect(container.querySelector('.multi-file-upload__remove-item').classList.contains('hidden')).toBeTrue();
                    done();
                });
            });

            describe('When the user removes the last error', () => {
                beforeEach((done) => {
                    spyOn(instance, 'requestRemoveFile').and.callFake((file) => {
                        instance.requestRemoveFileCompleted(file);
                    });
                    input.files = createFileList([new File([''], '/path/to/test.txt')]);
                    input.dispatchEvent(new Event('change'));
                    done();
                });

                it('Then the document title prefix ("Error:") should be removed', (done) => {
                    item.querySelector('.multi-file-upload__remove-item').click();
                    expect(document.title).not.toContain('Error:');
                    done();
                });
            });

            describe('When the user changes the file', () => {
                beforeEach((done) => {
                    input.files = createFileList([new File([''], '/path/to/test.txt')]);
                    input.dispatchEvent(new Event('change'));
                    done();
                });

                it('Then the input should no longer have the aria-describedby attribute', (done) => {
                    instance.requestUploadStatus.and.callFake((fileRef) => {
                        instance.handleRequestUploadStatusCompleted(fileRef, getStatusResponse());
                    });
                    input.dispatchEvent(new Event('change'));
                    expect(input.hasAttribute('aria-describedby')).toEqual(false);
                    done();
                });
            });
        });

        describe('And component is initialised', () => {
                    beforeEach((done) => {
                        instance = new MultiFileUpload(container);
                        spyOn(instance, 'requestProvisionUpload').and.callFake((file) => {
                            const response = getProvisionResponse();
                            const promise = Promise.resolve(response);

                            promise.then(() => {
                                instance.handleProvisionUploadCompleted(file, response);
                                done();
                            });

                            return promise;
                        });
                        spyOn(instance, 'setDuplicateInsetText').and.returnValue(Promise.resolve({}));
                        spyOn(instance, 'uploadFile').and.callFake((file) => {
                            instance.handleUploadFileCompleted(file.dataset.multiFileUploadFileRef);
                        });
                        spyOn(instance, 'requestUploadStatus').and.callFake((fileRef) => {
                            instance.handleRequestUploadStatusCompleted(fileRef, getStatusResponseWaiting());
                        });
                        spyOn(instance, 'delayedRequestUploadStatus').and.callFake((fileRef) => {
                            instance.requestUploadStatus(fileRef);
                        });

                        instance.init();
                        item = container.querySelector('.multi-file-upload__item');
                        input = container.querySelector('.multi-file-upload__file');
                        done();
                    });

                    describe('When there is an error', () => {
                        beforeEach((done) => {

                            input.files = createFileList([new File([''], '/path/to/test.txt')]);
                            input.dispatchEvent(new Event('change'));
                            done();
                        });

                        it('Then item should have the aria-describedby attribute', (done) => {
                            expect(input.hasAttribute('aria-describedby')).toEqual(true);
                            expect(input.getAttribute('aria-describedby')).toEqual('error-message-123');
                            done();
                        });

                        it('Then the document title should be prefixed with "Error:"', (done) => {
                            expect(document.title).toContain('Error:');
                            done();
                        });

                         it('Then the file could not be uploaded error is shown', (done) => {
                              expect(item.querySelector('.multi-file-upload__error-message').textContent).toEqual('File 1 could not be uploaded. Remove the file and try again.');
                              done();
                            });

                        it('Then the remove button is not present', (done) => {
                            expect(container.querySelector('.multi-file-upload__remove-item').classList.contains('hidden')).toBeTrue();
                            done();
                        });
                    });
                });

        describe('And component is initialised', () => {
            beforeEach((done) => {
                instance = new MultiFileUpload(container);
                spyOn(instance, 'requestProvisionUpload').and.callFake((file) => {
                    const response = getProvisionResponse();
                    const promise = Promise.resolve(response);

                    promise.then(() => {
                        instance.handleProvisionUploadCompleted(file, response);
                        done();
                    });

                    return promise;
                });
                spyOn(instance, 'setDuplicateInsetText').and.returnValue(Promise.resolve({}));
                spyOn(instance, 'uploadFile').and.callFake((file) => {
                    instance.handleUploadFileCompleted(file.dataset.multiFileUploadFileRef);
                });
                spyOn(instance, 'requestUploadStatus').and.callFake((fileRef) => {
                    instance.handleRequestUploadStatusCompleted(fileRef, getStatusResponse());
                });
                spyOn(instance, 'delayedRequestUploadStatus').and.callFake((fileRef) => {
                    instance.requestUploadStatus(fileRef);
                });

                instance.init();
                item = container.querySelector('.multi-file-upload__item');
                input = container.querySelector('.multi-file-upload__file');
                done();
            });

            describe('When file is uploaded', () => {
                beforeEach((done) => {

                    input.files = createFileList([new File([''], '/path/to/test.txt')]);
                    input.dispatchEvent(new Event('change'));
                    done();
                });

                it('Then item should be in "uploaded" state', (done) => {
                    expect(item.classList.contains('multi-file-upload__item--uploaded')).toEqual(true);
                    done();
                });

                it('Then "file uploaded" message is placed in aria live region', (done) => {
                    const notifications = container.querySelector('.multi-file-upload__notifications');
                    expect(notifications.textContent.trim()).toEqual('File 1 test.txt has been uploaded');
                    done();
                });

                it('Then the remove button is present', (done) => {
                    expect(container.querySelector('.multi-file-upload__remove-item').classList.contains('hidden')).toBeFalse();
                    done();
                });
                it('Then the remove button has fileName', (done) => {
                    expect(container.querySelector('.multi-file-upload__remove-item .multi-file-upload__file-name').textContent).toEqual('test.txt');
                    done();
                });
            });
        });

        describe('And there is one initially uploaded file', () => {
            beforeEach(() => {
                container.dataset.multiFileUploadUploadedFiles = JSON.stringify([{
                    fileStatus: 'READY',
                    reference: '123',
                    uploadDetails: {
                        fileName: 'test.txt'
                    }
                }]);
            });

            describe('When component is initialised', () => {
                beforeEach(() => {
                    instance = new MultiFileUpload(container);
                    spyOn(instance, 'setDuplicateInsetText').and.returnValue(Promise.resolve({}));
                    spyOn(instance, 'requestProvisionUpload').and.returnValue(Promise.resolve(getProvisionResponse()));

                    instance.init();

                    item = container.querySelector('.multi-file-upload__item');
                    item2 = container.querySelector('.multi-file-upload__item:nth-of-type(2)');
                    input = container.querySelector('.multi-file-upload__file');
                });

                it('Then first item should be in "uploaded" state', () => {
                    expect(item.classList.contains('multi-file-upload__item--uploaded')).toEqual(true);
                });

                it('Then input should have data prop multiFileUploadFileRef="123"', () => {
                    expect(input.dataset.multiFileUploadFileRef).toEqual('123');
                });

                it('Then fileName should contain "test.txt"', () => {
                    const fileName = container.querySelector('.multi-file-upload__file-name');
                    expect(fileName.textContent).toEqual('test.txt');
                });

                it('Then the remove button is present', (done) => {
                    expect(container.querySelector('.multi-file-upload__remove-item').classList.contains('hidden')).toBeFalse();
                    done();
                });

                it('Then the remove button has fileName', (done) => {
                    expect(container.querySelector('.multi-file-upload__remove-item .multi-file-upload__file-name').textContent).toEqual('test.txt');
                    done();
                });
            });

            describe('And component is initialised', () => {
                beforeEach(() => {
                    instance = new MultiFileUpload(container);

                    spyOn(instance, 'requestProvisionUpload').and.returnValue(Promise.resolve(getProvisionResponse()));
                    spyOn(instance, 'setDuplicateInsetText').and.returnValue(Promise.resolve({}));
                    spyOn(instance, 'redirectToFileRemovalPage');

                    instance.init();

                    item = container.querySelector('.multi-file-upload__item');
                    input = container.querySelector('.multi-file-upload__file');
                });

                describe('When file is removed', () => {
                    beforeEach(() => {
                        item.querySelector('.multi-file-upload__remove-item').click();
                    });

                    it('Then the user should be redirected to the remove file page', () => {
                        expect(instance.redirectToErrorServicePage.toHaveBeenCalled);
                    });
                });
            });
        });
    });
});