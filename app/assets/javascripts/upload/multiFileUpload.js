"use strict";

const status = {
    Default: "Default",
    Waiting: "Waiting",
    Uploading: "Uploading",
    Verifying: "Verifying",
    Uploaded: "Uploaded",
    Removing: "Removing"
}

export class MultiFileUpload {
    /**  F1 */
    constructor(form) {
        this.uploadData = {};
        this.container = form;
        this.lastFileIndex = 0;
        this.errorMessageTpl = "";
        this.errorSummaryList = "";
        this.errorSummaryTpl = "";
        this.errorSummaryItemTpl = "";
        this.errorSummary = "";
        this.hasDisplayedErrorPrefix = false;
        this.errors = {};
        this.csrfToken = form.querySelector("input[name=csrfToken]").value;
        this.config = {
            startRows: parseInt(form.dataset.multiFileUploadStartRows) || 1,
            minFiles: parseInt(form.dataset.multiFileUploadMinFiles) || 1,
            maxFiles: parseInt(form.dataset.multiFilUeploadMaxFiles) || 5,
            uploadedFiles: form.dataset.multiFileUploadUploadedFiles ? JSON.parse(form.dataset.multiFileUploadUploadedFiles) : [],
            maxRetries: parseInt(form.dataset.multiFileUploadMaxRetries) || 30,
            retryDelayMs: parseInt(form.dataset.multiFileUploadRetryDelayMs, 10) || 1000,
            sendUrlTpl: decodeURIComponent(form.dataset.multiFileUploadSendUrlTpl),
            statusUrlTpl: decodeURIComponent(form.dataset.multiFileUploadStatusUrlTpl),
            removeUrlTpl: decodeURIComponent(form.dataset.multiFileUploadRemoveUrlTpl),
            getDuplicateUrlTpl: decodeURIComponent(form.dataset.multiFileUploadDuplicateUrlTpl),
            getErrorServiceUrlTpl: decodeURIComponent(form.dataset.multiFileUploadErrorUrlTpl)
        }

        this.classes = {
            itemList: 'multi-file-upload__item-list',
            item: 'multi-file-upload__item',
            file: 'multi-file-upload__file',
            fileName: 'multi-file-upload__file-name',
            fileNumber: 'multi-file-upload__number',
            addAnother: 'multi-file-upload__add-another',
            waiting: 'multi-file-upload__item--waiting',
            uploading: 'multi-file-upload__item--uploading',
            progressBar: 'multi-file-upload__progress-bar',
            filePreview: 'multi-file-upload__file-preview',
            removing: 'multi-file-upload__item--removing',
            verifying: 'multi-file-upload__item--verifying',
            uploaded: 'multi-file-upload__item--uploaded',
            remove: 'multi-file-upload__remove-item',
            inputContainer: 'govuk-form-group',
            inputContainerError: 'govuk-form-group--error',
            label: 'govuk-label',
            errorSummaryList: 'govuk-error-summary__list',
            notifications: 'multi-file-upload__notifications',
            errorSummary: 'govuk-error-summary',
            formStatus: 'multi-file-upload__form-status',
            inset: 'govuk-inset-text'
        }

        this.messages = {
            genericError: form.dataset.multiFileUploadErrorGeneric,
            stillTransferring: form.dataset.multiFileUploadStillTransferring,
            fileUploaded: form.dataset.multiFileUploadFileUploaded,
            fileUploading: form.dataset.multiFileUploadFileUploading,
            fileRemoved: form.dataset.multiFileUploadFileRemoved,
            errorPrefix: form.dataset.multiFileUploadErrorPrefix
        }

        this.cacheTemplates();
        this.cacheElements();
        this.bindEvents();
    }

    /** F2 */
    cacheElements() {
        this.itemList = this.container.querySelector(`.${this.classes.itemList}`);
        this.addAnotherBtn = this.container.querySelector(`.${this.classes.addAnother}`);
        this.formStatus = this.container.querySelector(`.${this.classes.formStatus}`);
        this.submitBtn = this.container.querySelector(`.${this.classes.submitBtn}`);
        this.errorSummary = this.parseHtml(this.errorSummaryTpl, {});
        this.errorSummaryList = this.errorSummary.querySelector(`.${this.classes.errorSummaryList}`);
        this.notifications = this.container.querySelector(`.${this.classes.notifications}`);
        this.inset = this.container.querySelector(`.${this.classes.inset}`);

    }

    /** F3 */
    cacheTemplates() {
        this.errorSummaryTpl = document.getElementById('error-manager-summary-tpl').textContent;
        this.itemTpl = document.getElementById('multi-file-upload-item-tpl').textContent;
        this.errorMessageTpl = document.getElementById('error-manager-message-tpl').textContent;
        this.errorSummaryItemTpl = document.getElementById('error-manager-summary-item-tpl').textContent;
    }

    /** F4 */
    bindEvents() {
        this.addAnotherBtn.addEventListener('click', this.handleAddItem.bind(this));
        this.container.addEventListener('submit', this.handleSubmit.bind(this));
    }

    /** F5 */
    bindItemEvents(item) {
        this.getRemoveButtonFromItem(item).addEventListener('click', this.handleRemoveItem.bind(this));
    }

    /** F6 */
    bindUploadEvents(item) {
        this.getFileFromItem(item).addEventListener('change', this.handleFileChange.bind(this));
    }

    /** F7 */
    handleFileChange(e) {
        const file = e.target;
        const item = this.getItemFromFile(file);
        this.removeError(file.id);

        if (!file.files.length) {
            return;
        }
        item.querySelector(`.${this.classes.fileName}`).style.display = null;
        this.getFileNameElement(item).textContent = this.extractFileName(file.value);
        this.setItemState(item, status.Waiting);
        this.uploadNext();
    }

    /** F8 */
    handleRemoveItem(e) {
        const target = e.target;
        const item = target.closest(`.${this.classes.item}`);
        const file = this.getFileFromItem(item);
        const ref = file.dataset.multiFileUploadFileRef;
        if (this.isUploading(item)) {
            if (this.uploadData[file.id].uploadHandle) {
                this.uploadData[file.id].uploadHandle.abort();
            }
        }

        if (ref) {
            this.setItemState(item, status.Removing);
            this.requestRemoveFile(file, false);
        } else {
            this.requestRemoveFile(file, false);
            this.removeItem(item);
        }
    }

    /** F9 */
    handleSubmit(e) {
        e.preventDefault();

        this.updateFormStatusVisibility(this.isBusy());

        if (this.hasErrors()) {
            document.querySelector(`.${this.classes.errorSummary}`).focus();
            return;
        }

        if (this.isInProgress()) {
            this.addNotification(this.messages.stillTransferring);

            return;
        }
        window.location.href = this.container.action;
    }

    /** F10 */
    isInProgress() {
        const stillWaiting = this.container.querySelector(`.${this.classes.waiting}`) !== null;

        return stillWaiting || this.isBusy();
    }

    /** F11 */
    addNotification(message) {
        const element = document.createElement('p');
        element.textContent = message;

        this.notifications.append(element);

        window.setTimeout(() => {
            element.remove();
        }, 1000);
    }

    /** F12 */
    hasErrors() {
        return Object.entries(this.errors).length > 0;
    }

    /** F13 */
    requestRemoveFile(file, isInit) {
        let item;
        let ref;
        if (!isInit) {
            item = this.getItemFromFile(file);
            ref = file.dataset.multiFileUploadFileRef;
        } else {
            ref = file.reference;
        }
        const formData = new FormData();
        formData.append('csrfToken', this.csrfToken);

        fetch(this.getRemoveUrl(ref), {
            method: 'POST',
            body: formData
        })
            .then(this.requestRemoveFileCompleted.bind(this, file, isInit))
            .then(this.setDuplicateInsetText.bind(this))
            .catch(() => {
                if (!isInit) {
                    this.setItemState(item, status.Uploaded);
                }
            });
    }

    /** F14 */
    requestRemoveFileCompleted(file, isInit) {
        if(!isInit) {
            const item = file.closest(`.${this.classes.item}`);
            if(this.getFileName(file)) {
                this.addNotification(this.parseTemplate(this.messages.fileRemoved, {
                    fileNumber: item.querySelector(".govuk-label").textContent,
                    fileName: this.getFileName(file)
                }));
            }
            this.removeItem(item);
        }
    }

    /** F15 */
    removeItem(item) {
        const file = this.getFileFromItem(item);
        this.removeError(file.id);
        item.remove();
        this.updateFileNumbers();
        this.updateButtonVisibility();
        this.updateFormStatusVisibility();

        if (this.getItems().length === 0) {
            this.addItem(true, true);
        }

        delete this.uploadData[file.id];

        this.uploadNext();
    }

    /** F16 */
    updateFileNumbers() {
        let fileNumber = 1;

        this.getItems().forEach(item => {
            Array.from(item.querySelectorAll(`.${this.classes.fileNumber}`)).forEach(span => {
                span.textContent = fileNumber.toString();
            });

            fileNumber++;
        });
    }

    /** F17 */
    getFileNameElement(item) {
        return item.querySelector(`.${this.classes.fileName}`);
    }

    /** F18 */
    setItemState(item, uploadState) {
        const file = this.getFileFromItem(item);
        item.classList.remove(this.classes.waiting, this.classes.uploading, this.classes.verifying, this.classes.uploaded, this.classes.removing);

        file.disabled = uploadState !== status.Default;
        switch (uploadState) {
            case status.Waiting:
                item.classList.add(this.classes.waiting);
                break;
            case status.Uploading:
                item.classList.add(this.classes.uploading);
                break;
            case status.Verifying:
                item.classList.add(this.classes.verifying);
                break;
            case status.Uploaded:
                item.classList.add(this.classes.uploaded);
                break;
            case status.Removing:
                item.classList.add(this.classes.removing);
                break;
        }
    }

    /** F19 */
    uploadNext() {
        const nextItem = this.itemList.querySelector(`.${this.classes.waiting}`);
        if (!nextItem || this.isBusy()) {
            return;
        }

        const file = this.getFileFromItem(nextItem);
        this.setItemState(nextItem, status.Uploading);
        this.provisionUpload(file, false);
    }

    /** F20 */
    extractFileName(fileName) {
        return fileName.split(/([\\/])/g).pop();
    }

    /** F21 */
    getFileName(file) {
        const item = this.getItemFromFile(file);
        const fileName = this.getFileNameElement(item).textContent.trim();

        if (fileName.length) {
            return this.extractFileName(fileName);
        }

        if (file.value.length) {
            return this.extractFileName(file.value);
        }

        return null;
    }

    /** F22 */
    prepareFileUpload(file) {
        const item = this.getItemFromFile(file);
        const fileName = this.getFileName(file);

        this.updateButtonVisibility();
        this.removeError(file.id);

        this.getFileNameElement(item).textContent = fileName;

        this.uploadData[file.id].uploadHandle = this.uploadFile(file);
    }

    /** F23 */
    prepareFormData(file, data) {
        const formData = new FormData();

        for (const [key, value] of Object.entries(data.fields)) {
            formData.append(key, value);
        }

        formData.append('file', file.files[0]);

        return formData;
    }

    /** F77 */
    setUploadingMessage(item, file) {
        item.querySelector(`.${this.classes.progressBar}`).textContent = this.parseTemplate(this.messages.fileUploading, {
            fileNumber: item.querySelector(".govuk-label").textContent.toLowerCase(),
            fileName: this.getFileName(file)
        });
    }
    /** F24 */
    uploadFile(file) {
        const xhr = new XMLHttpRequest();
        const fileRef = file.dataset.multiFileUploadFileRef;
        const data = this.uploadData[file.id];
        const formData = this.prepareFormData(file, data);
        const item = this.getItemFromFile(file);
        this.setUploadingMessage(item, file);
        xhr.upload.addEventListener('progress', this.handleUploadFileProgress.bind(this, item));
        xhr.addEventListener('load', this.handleUploadFileCompleted.bind(this, fileRef));
        xhr.addEventListener('error', this.handleUploadFileError.bind(this, fileRef));
        xhr.open('POST', data.url);
        xhr.send(formData);

        return xhr;
    }

    /** F25 */
    handleUploadFileProgress(item, e) {
        if (e.lengthComputable) {
            this.updateUploadProgress(item, e.loaded / e.total * 95);
        }
    }

    /** F26 */
    updateUploadProgress(item, value) {
        item.querySelector(`.${this.classes.progressBar}`).style.width = `${value}%`;
    }

    /** F27 */
    getFileByReference(fileRef) {
        return this.itemList.querySelector(`[data-multi-file-upload-file-ref="${fileRef}"]`);
    }

    /** F28 */
    handleUploadFileCompleted(fileRef) {
        const file = this.getFileByReference(fileRef);
        const item = this.getItemFromFile(file);

        this.setItemState(item, status.Verifying);
        this.delayedRequestUploadStatus(fileRef);
    }

    /** F29 */
    handleUploadFileError(fileRef) {
        const file = this.getFileByReference(fileRef);
        const item = this.getItemFromFile(file);
        item.querySelector(`.${this.classes.fileName}`).style.display = "none";
        this.updateFormStatusVisibility();
        this.addError(file, this.messages.genericError);
    }

    /** F30 */
    handleFileStatusFailed(file, errorMessage) {
        const item = this.getItemFromFile(file);
        item.querySelector(`.${this.classes.fileName}`).style.display = "none";
        this.setItemState(item, status.Default);
        this.addError(file.id, errorMessage);
        this.updateFormStatusVisibility();
    }

    /** F31 */
    addError(inputId, message) {
        this.removeError(inputId);

        const errorMessage = this.addErrorToField(inputId, message);
        const errorSummaryRow = this.addErrorToSummary(inputId, message);

        this.errors[inputId] = {
            errorMessage: errorMessage,
            errorSummaryRow: errorSummaryRow
        };
        this.updateErrorSummaryVisibility();
        document.querySelector('.govuk-error-summary').focus();
    }

    /** F32 */
    removeError(inputId) {
        if (!Object.prototype.hasOwnProperty.call(this.errors, inputId)) {
            return;
        }

        const error = this.errors[inputId];
        const input = document.getElementById(inputId);
        const inputContainer = this.getContainer(input);
        input.removeAttribute('aria-describedby');
        error.errorMessage.remove();
        error.errorSummaryRow.remove();

        inputContainer.classList.remove(this.classes.inputContainerError);

        delete this.errors[inputId];

        this.updateErrorSummaryVisibility();
    }

    /** F33 */
    updateErrorSummaryVisibility() {
        if (this.hasErrors()) {
            document.querySelector('#penalty-information').before(this.errorSummary);
            if(!this.hasDisplayedErrorPrefix) {
                document.title = this.messages.errorPrefix + ' ' + document.title;
                this.hasDisplayedErrorPrefix = true;
            }
        } else {
            document.title = document.title.split(": ")[1];
            this.hasDisplayedErrorPrefix = false;
            this.errorSummary.remove();
        }
    }

    /** F34 */
    addErrorToField(inputId, message) {
        const input = document.getElementById(inputId);
        const inputContainer = this.getContainer(input);
        const label = this.getLabel(inputContainer);
        const file = this.getFileFromItem(inputContainer);
        const ref = file.dataset.multiFileUploadFileRef;
        const errorMessage = this.parseHtml(this.errorMessageTpl, {
            fileRef: ref,
            errorMessage: message
        });
        inputContainer.classList.add(this.classes.inputContainerError);
        input.setAttribute('aria-describedby', `error-message-${ref}`);
        label.after(errorMessage);

        return errorMessage;
    }

    /** F35 */
    getContainer(input) {
        return input.closest(`.${this.classes.inputContainer}`);
    }

    /** F36 */
    getLabel(container) {
        return container.querySelector(`.${this.classes.label}`);
    }

    /** F37 */
    addErrorToSummary(inputId, message) {
        const summaryRow = this.parseHtml(this.errorSummaryItemTpl, {
            inputId: inputId,
            errorMessage: message
        });

        this.bindErrorEvents(summaryRow, inputId);
        this.errorSummaryList.append(summaryRow);

        return summaryRow;
    }

    /** F38 */
    bindErrorEvents(errorItem, inputId) {
        errorItem.querySelector('a').addEventListener('click', (e) => {
            e.preventDefault();

            document.getElementById(inputId).focus();
        });
    }

    /** F39 */
    delayedRequestUploadStatus(fileRef) {
        window.setTimeout(this.requestUploadStatus.bind(this, fileRef), this.config.retryDelayMs);
    }

    /** F40 */
    requestUploadStatus(fileRef) {
        const file = this.getFileByReference(fileRef);
        if (!file || !Object.prototype.hasOwnProperty.call(this.uploadData, file.id) || this.uploadData[file.id].uploaded) {
            return;
        }

        fetch(this.getStatusUrl(fileRef), {
            method: 'GET'
        })
            .then(response => response.json())
            .then(this.handleRequestUploadStatusCompleted.bind(this, fileRef))
            .catch(this.delayedRequestUploadStatus.bind(this, fileRef));
    }

    /** F41 */
    handleRequestUploadStatusCompleted(fileRef, response) {
        const file = this.getFileByReference(fileRef);
        const data = this.uploadData[file.id];
        const fileStatus = response['status'];
        const error = response['errorMessage'];
        switch (fileStatus) {
            case 'READY':
                this.uploadData[file.id].uploaded = true;
                this.handleFileStatusSuccessful(file);
                this.uploadNext();
                break;
            case 'DUPLICATE':
                this.showInsetText({'message': error});
                this.handleFileStatusSuccessful(file);
                this.uploadNext();
                break;
            case 'FAILED':
            case 'REJECTED':
            case 'QUARANTINE':
                this.handleFileStatusFailed(file, error);

                this.uploadNext();
                break;
            default:
                data.retries++;

                if (data.retries > this.config.maxRetries) {
                    this.uploadData[file.id].retries = 0;
                    this.handleFileStatusFailed(file, this.messages.genericError);
                    this.uploadNext();
                } else {
                    this.delayedRequestUploadStatus(fileRef);
                }

                break;
        }
    }

    /** F42 */
    handleFileStatusSuccessful(file) {
        const item = this.getItemFromFile(file);
        this.addNotification(this.parseTemplate(this.messages.fileUploaded, {
            fileNumber: item.querySelector(".govuk-label").textContent,
            fileName: this.getFileName(file)
        }));
        this.setItemState(item, status.Uploaded);
        this.updateButtonVisibility();
        this.updateFormStatusVisibility();
    }

    /** F43 */
    updateFormStatusVisibility(forceState = undefined) {
        if (forceState !== undefined) {
            this.toggleElement(this.formStatus, forceState);
        } else if (!this.isBusy()) {
            this.toggleElement(this.formStatus, false);
        }
    }

    /** F45 */
    getSendUrl() {
        return this.config.sendUrlTpl;
    }

    /** F46 */
    getRemoveUrl(fileRef) {
        return this.parseTemplate(this.config.removeUrlTpl, {fileRef: fileRef});
    }

    /** F47 */
    requestProvisionUpload(file) {
        const formData = new FormData();
        formData.append('csrfToken', this.csrfToken);

        return fetch(this.getSendUrl(), {
            method: 'POST',
            body: formData
        })
            .then(response => {
                              if (response.status == 500) {
                              this.redirectToErrorServicePage();
                              } else {
                                return response.json();
                              }
                            })
            .then(this.handleProvisionUploadCompleted.bind(this, file))
            .catch(this.delayedProvisionUpload.bind(this, file));
    }

    /** F48 */
    delayedProvisionUpload(file) {
        window.setTimeout(this.provisionUpload.bind(this, file), this.config.retryDelayMs);
    }

    /** F49 */
    handleProvisionUploadCompleted(file, response) {
        const fileRef = response['reference'];

        file.dataset.multiFileUploadFileRef = fileRef;
        this.uploadData[file.id].reference = fileRef;
        this.uploadData[file.id].fields = response['uploadRequest']['fields'];
        this.uploadData[file.id].url = response['uploadRequest']['href'];
        this.uploadData[file.id].retries = 0;
    }

    /** F50 */
    provisionUpload(file, placeholder) {
        const item = this.getItemFromFile(file);

        if (Object.prototype.hasOwnProperty.call(this.uploadData, file.id) && !placeholder) {
            this.prepareFileUpload(file);

            return;
        }

        this.uploadData[file.id] = {};
        this.uploadData[file.id].provisionPromise = this.requestProvisionUpload(file);

        this.uploadData[file.id].provisionPromise.then(() => {
            if (item.parentNode !== null && !placeholder) {
                this.prepareFileUpload(file);
            }
        });
    }

    /** F51 */
    init() {
        this.updateButtonVisibility();
        this.removeAllItems();
        this.createInitialRows();
    }

    /** F52 */
    createInitialRows() {
        let rowCount = 0;
        this.config.uploadedFiles.filter(file => file['fileStatus'] === 'WAITING' || file['fileStatus'] === 'FAILED').forEach(fileData => {
            this.requestRemoveFile(fileData, true);
        });
        this.config.uploadedFiles = this.config.uploadedFiles.filter(file => file['fileStatus'] !== 'WAITING' && file['fileStatus'] !== 'FAILED');
        this.config.uploadedFiles.filter(file => file['fileStatus'] === 'READY' || file['fileStatus'] === 'DUPLICATE').forEach(fileData => {
            this.createUploadedItem(fileData);
            rowCount++;
        });
        if (rowCount < this.config.startRows) {
            for (let a = rowCount; a < this.config.startRows; a++) {
                this.addItem(true, this.config.uploadedFiles.length === 0);
            }
        }
        this.setDuplicateInsetText();
    }

    /** F53 */
    createUploadedItem(fileData) {
        const item = this.addItem(true, false);
        const file = this.getFileFromItem(item);
        const fileName = this.extractFileName(fileData['uploadDetails']['fileName']);

        this.setItemState(item, status.Uploaded);
        this.getFileNameElement(item).textContent = fileName;
        this.toggleRemoveButtons(true);
        file.dataset.multiFileUploadFileRef = fileData['reference'];

        return item;
    }

    /** F54 */
    isBusy() {
        const stillUploading = this.container.querySelector(`.${this.classes.uploading}`) !== null;
        const stillVerifying = this.container.querySelector(`.${this.classes.verifying}`) !== null;
        const stillRemoving = this.container.querySelector(`.${this.classes.removing}`) !== null;

        return stillUploading || stillVerifying || stillRemoving;
    }

    /** F55 */
    handleAddItem() {
        const item = this.addItem(false, false);
        const file = this.getFileFromItem(item);

        file.focus();
    }

    /** F56 */
    addItem(isInit, isNoExistingUploads) {
        const item = this.parseHtml(this.itemTpl, {
            fileNumber: (this.getItems().length + 1).toString(),
            fileIndex: (++this.lastFileIndex).toString()
        });

        this.bindUploadEvents(item);
        this.bindItemEvents(item);
        this.itemList.append(item);
        this.updateButtonVisibility();
        if (!isInit || isNoExistingUploads) {
            const file = this.getFileFromItem(item);
            this.provisionUpload(file, true);
        }
        return item;
    }

    /** F57 */
    getStatusUrl(fileRef) {
        return this.parseTemplate(this.config.statusUrlTpl, {fileRef: fileRef});
    }

    /** F58 */
    parseHtml(string, model) {
        string = this.parseTemplate(string, model);

        const dp = new DOMParser();
        const doc = dp.parseFromString(string, 'text/html');

        return doc.body.firstChild;
    }

    /** F59 */
    parseTemplate(string, model) {
        for (const [key, value] of Object.entries(model)) {
            string = string.replace(new RegExp('{' + key + '}', 'g'), value.toString());
        }
        return string;
    }

    /** F60 */
    getItems() {
        return Array.from(this.itemList.querySelectorAll(`.${this.classes.item}`));
    }

    /** F61 */
    getFileFromItem(item) {
        return item.querySelector(`.${this.classes.file}`);
    }

    /** F62 */
    updateButtonVisibility() {
        const itemCount = this.getItems().length;
        this.toggleRemoveButtons(itemCount > this.config.minFiles);
        this.toggleAddButton(itemCount < this.config.maxFiles);
    }

    /** F63 */
    toggleAddButton(state) {
        this.toggleElement(this.addAnotherBtn, state);
    }

    /** F64 */
    toggleRemoveButtons(state) {
        this.getItems().forEach(item => {
            const button = this.getRemoveButtonFromItem(item);

            if (this.isWaiting(item) || this.isUploading(item) || this.isVerifying(item) || this.isUploaded(item) || this.hasErrors()) {
                state = true;
            }

            this.toggleElement(button, state);
        });
    }

    /** F65 */
    toggleElement(element, state) {
        element.classList.toggle('hidden', !state);
    }

    /** F66 */
    removeAllItems() {
        this.getItems().forEach(item => item.remove());
    }

    /** F67 */
    getItemFromFile(file) {
        return file.closest(`.${this.classes.item}`);
    }

    /** F68 */
    getRemoveButtonFromItem(item) {
        return item.querySelector(`.${this.classes.remove}`);
    }

    /** F69 */
    isUploading(item) {
        return item.classList.contains(this.classes.uploading);
    }

    /** F70 */
    isWaiting(item) {
        return item.classList.contains(this.classes.waiting);
    }

    /** F71 */
    isVerifying(item) {
        return item.classList.contains(this.classes.verifying);
    }

    /** F72 */
    isUploaded(item) {
        return item.classList.contains(this.classes.uploaded);
    }

    /** F73 */
    setDuplicateInsetText() {
        fetch(this.getDuplicateUrl(), {
            method: 'GET'
        })
            .then(response => response.json())
            .then(this.showInsetText.bind(this))
    }

    /** F74 */
    getDuplicateUrl() {
        return this.config.getDuplicateUrlTpl;
    }

    /** F75 */
    showInsetText(response) {
        const message = response['message'];
        if (message === undefined) {
            this.container.querySelector(".govuk-inset-text").classList.add("hidden");
            this.container.querySelector(".govuk-inset-text").ariaHidden = "true";
        } else {
            this.container.querySelector(".govuk-inset-text").classList.remove("hidden");
            this.container.querySelector(".govuk-inset-text").innerHTML = message;
            this.container.querySelector(".govuk-inset-text").ariaHidden = "false";
        }
    }

    /** F76 */
        getErrorServiceUrl() {
            return this.config.getErrorServiceUrlTpl;
        }

    /** F77 */
        redirectToErrorServicePage() {
             window.location.href = this.getErrorServiceUrl();
         }
}

/**
 * Adds event listener on page load, gets the form by its ID and then creates an MultiFileUpload with the form stored within.
 * Initialises the base state of the MultiFileUpload object.
 */
document.addEventListener('DOMContentLoaded', function () {
    const element = document.getElementById("multi-upload-form");
    if(element) {
        const uploadObj = new MultiFileUpload(element);
        uploadObj.init();
    }
});
