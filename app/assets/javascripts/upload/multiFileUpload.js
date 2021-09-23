"use strict";

const status = {
    Default: "Default",
    Waiting: "Waiting",
    Uploading: "Uploading",
    Verifying: "Verifying",
    Uploaded: "Uploaded"
}

class MultiFileUpload {
    /**
     * Constructs an object - setting all config derived from the form's attributeextractFileNames and class names to be used in functions.
     * Caches the elements and templates to use in later functions.
     * Binds an event listener to the 'Add another document' button to add another file.
     *
     * @param form Form element
     */
    constructor(form) {
        this.uploadData = {};
        this.container = form;
        this.lastFileIndex = 0;
        this.config = {
            startRows: parseInt(form.dataset.multiFileUploadStartRows) || 1,
            minFiles: parseInt(form.dataset.multiFileUploadMinFiles) || 1,
            maxFiles: parseInt(form.dataset.multiFilUeploadMaxFiles) || 5,
            uploadedFiles: form.dataset.multiFileUploadUploadedFiles ? JSON.parse(form.dataset.multiFileUploadUploadedFiles) : [],
            maxRetries: parseInt(form.dataset.multiFileUploadMaxRetries) || 30,
            retryDelayMs: parseInt(form.dataset.multiFileUploadRetryDelayMs, 10) || 1000,
            sendUrlTpl: decodeURIComponent(form.dataset.multiFileUploadSendUrlTpl),
            statusUrlTpl: decodeURIComponent(form.dataset.multiFileUploadStatusUrlTpl)
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
            verifying: 'multi-file-upload__item--verifying',
            uploaded: 'multi-file-upload__item--uploaded'
        }

        this.cacheElements();
        this.cacheTemplates();
        this.bindEvents();
    }

    /**
     * Stores the current state of itemList (files uploaded), the 'add another document' button and others.
     *
     */
    cacheElements() {
        this.itemList = this.container.querySelector(`.${this.classes.itemList}`);
        this.addAnotherBtn = this.container.querySelector(`.${this.classes.addAnother}`);
        this.formStatus = this.container.querySelector(`.${this.classes.formStatus}`);
        this.submitBtn = this.container.querySelector(`.${this.classes.submitBtn}`);
    }

    /**
     * Stores the template of each upload 'row'/'item' so it can construct new items dynamically.
     *
     */
    cacheTemplates() {
        this.itemTpl = document.getElementById('multi-file-upload-item-tpl').textContent;
    }

    /**
     * Binds the click event to the 'Add another document' button to add more items when clicked.
     *
     */
    bindEvents() {
        this.addAnotherBtn.addEventListener('click', this.handleAddItem.bind(this));
    }

    /**
     * Binds the change event to the specified item so that any file change is handled.
     *
     */
    bindUploadEvents(item) {
        this.getFileFromItem(item).addEventListener('change', this.handleFileChange.bind(this));
    }

    /**
     * Gets the item from the file (through the event), extracts the file name and attempts to upload the file.
     *
     */
    handleFileChange(e)  {
        const file = e.target;
        const item = this.getItemFromFile(file);

        if (!file.files.length) {
            return;
        }

        this.getFileNameElement(item).textContent = this.extractFileName(file.value);
        this.setItemState(item, status.Waiting);
        this.uploadNext();
    }

    /**
     * Gets the file name element.
     *
     */
    getFileNameElement(item) {
        return item.querySelector(`.${this.classes.fileName}`);
    }

    /**
     * Removes any state the item is currently in and sets the new state. Uses a specified class name to apply
     * specific styling.
     *
     */
    setItemState(item, uploadState) {
        const file = this.getFileFromItem(item);
        item.classList.remove(this.classes.waiting, this.classes.uploading, this.classes.verifying, this.classes.uploaded);

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
        }
    }

    /**
     * Gets the next 'Waiting' item to upload and checks to make sure we aren't currently uploading.
     * If there is no items to upload, or the system is busy, then nothing happens.
     * Otherwise, set the items state to 'Uploading' and begin upload process.
     *
     */
    uploadNext() {
        const nextItem = this.itemList.querySelector(`.${this.classes.waiting}`);

        if (!nextItem || this.isBusy()) {
            return;
        }

        const file = this.getFileFromItem(nextItem);

        this.setItemState(nextItem, status.Uploading);
        this.provisionUpload(file);
    }

    /**
     * Extracts the file name.
     *
     */
    extractFileName(fileName) {
        return fileName.split(/([\\/])/g).pop();
    }

    /**
     * Get the file name from a specified file.
     *
     */
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

    /**
     * Gets the item to upload from the file and sets the objects upload handle to the XHR returned.
     *
     */
    prepareFileUpload(file) {
        const item = this.getItemFromFile(file);
        const fileName = this.getFileName(file);

        this.updateButtonVisibility();

        this.getFileNameElement(item).textContent = fileName;

        this.uploadData[file.id].uploadHandle = this.uploadFile(file);
    }

    prepareFormData(file, data) {
        const formData = new FormData();

        for (const [key, value] of Object.entries(data.fields)) {
            formData.append(key, value);
        }

        formData.append('file', file.files[0]);

        return formData;
    }

    /**
     * Gets the item to upload and sends a request to the specified endpoint to upload the file.
     *
     */
    uploadFile(file) {
        const xhr = new XMLHttpRequest();
        const fileRef = file.dataset.multiFileUploadFileRef;
        const data = this.uploadData[file.id];
        const formData = this.prepareFormData(file, data);
        const item = this.getItemFromFile(file);

        xhr.upload.addEventListener('progress', this.handleUploadFileProgress.bind(this, item));
        xhr.addEventListener('load', this.handleUploadFileCompleted.bind(this, fileRef));
        xhr.open('POST', data.url);
        xhr.send(formData);

        return xhr;
    }

    /**
     *  Updates the file upload progression based on its upload state.
     *
     */
    handleUploadFileProgress(item, e) {
        if (e.lengthComputable) {
            this.updateUploadProgress(item, e.loaded / e.total * 95);
        }
    }

    /**
     *  Updates the view with the current state of upload.
     *
     */
    updateUploadProgress(item, value) {
        item.querySelector(`.${this.classes.progressBar}`).style.width = `${value}%`;
    }

    /**
     * Gets the file by the reference given by the upload service.
     */
    getFileByReference(fileRef) {
        return this.itemList.querySelector(`[data-multi-file-upload-file-ref="${fileRef}"]`);
    }


    /**
     * Sets the upload item's state to 'Verifying' and delays a request to check the upload status for this file.
     *
     */
    handleUploadFileCompleted(fileRef) {
        const file = this.getFileByReference(fileRef);
        const item = this.getItemFromFile(file);

        this.setItemState(item, status.Verifying);
        this.delayedRequestUploadStatus(fileRef);
    }

    /**
     * Wait a sufficient amount of time before requesting the status of the upload.
     */
    delayedRequestUploadStatus(fileRef) {
        window.setTimeout(this.requestUploadStatus.bind(this, fileRef), this.config.retryDelayMs);
    }

    /**
     * Fetches the current state of upload from the upload service, delaying the next call if the file has not been uploaded, or handling
     * the successful upload.
     */
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

    /**
     * Handles the response given by the upload service, it should do other things when implementing error scenarios.
     * On success, it set the items state to uploaded and proceeds to upload the next file.
     */
     handleRequestUploadStatusCompleted(fileRef, response) {
        const file = this.getFileByReference(fileRef);
        const data = this.uploadData[file.id];

        switch (response) {
            case 'READY':
                this.uploadData[file.id].uploaded = true;
                this.handleFileStatusSuccessful(file);
                this.uploadNext();
                break;
            default:
                data.retries++;

                if (data.retries > this.config.maxRetries) {
                    this.uploadData[file.id].retries = 0;

                    this.uploadNext();
                }
                else {
                    this.delayedRequestUploadStatus(fileRef);
                }

                break;
        }
    }

    /**
     * Sets the upload item to be in a successful/completed state.
     */
    handleFileStatusSuccessful(file) {
        const item = this.getItemFromFile(file);

        this.setItemState(item, status.Uploaded);
        this.updateButtonVisibility();
        this.updateFormStatusVisibility();
    }

    updateFormStatusVisibility(forceState = undefined) {
        if (forceState !== undefined) {
            this.toggleElement(this.formStatus, forceState);
        }
        else if (!this.isBusy()) {
            this.toggleElement(this.formStatus, false);
        }
    }

    /**
     * Returns the element of the file preview class selector.
     */
    getFilePreviewElement(item) {
        return item.querySelector(`.${this.classes.filePreview}`);
    }

    /**
     * Gets the URL to send the file to.
     */
    getSendUrl() {
        return this.config.sendUrlTpl;
    }

    /**
     * Requests information from the upload service to handle the upload request.
     */
    requestProvisionUpload(file) {
        return fetch(this.getSendUrl(), {
            method: 'POST'
        })
            .then(response => response.json())
            .then(this.handleProvisionUploadCompleted.bind(this, file))
            .catch(this.delayedProvisionUpload.bind(this, file));
    }

    /**
     * Delays the upload provisioning by a specified amount of time.
     */
    delayedProvisionUpload(file) {
        window.setTimeout(this.provisionUpload.bind(this, file), this.config.retryDelayMs);
    }

    /**
     * Handles a successful call to provision the file to upload and extracts relevant data from the JSON.
     */
    handleProvisionUploadCompleted(file, response) {
        const fileRef = response['reference'];

        file.dataset.multiFileUploadFileRef = fileRef;

        this.uploadData[file.id].reference = fileRef;
        this.uploadData[file.id].fields = response['uploadRequest']['fields'];
        this.uploadData[file.id].url = response['uploadRequest']['href'];
        this.uploadData[file.id].retries = 0;
    }

    /**
     * Provisions and prepares file for upload.
     */
    provisionUpload(file) {
        const item = this.getItemFromFile(file);

        if (Object.prototype.hasOwnProperty.call(this.uploadData, file.id)) {
            this.prepareFileUpload(file);

            return;
        }

        this.uploadData[file.id] = {};
        this.uploadData[file.id].provisionPromise = this.requestProvisionUpload(file);

        this.uploadData[file.id].provisionPromise.then(() => {
            if (item.parentNode !== null) {
                this.prepareFileUpload(file);
            }
        });
    }

    /**
     * Initialises the base state of the object, usually on page load.
     */
    init() {
        this.updateButtonVisibility();
        this.removeAllItems();
        this.createInitialRows();
    }

    /**
     * Creates the initial rows on page load.
     */
    createInitialRows() {
        let rowCount = 0;
         this.config.uploadedFiles.filter(file => file['fileStatus'] === 'READY').forEach(fileData => {
             this.createUploadedItem(fileData);
             rowCount++;
         });

        if (rowCount < this.config.startRows) {
            for (let a = rowCount; a < this.config.startRows; a++) {
                this.addItem();
            }
        }
    }

    createUploadedItem(fileData) {
        const item = this.addItem();
        const file = this.getFileFromItem(item);
        const fileName = this.extractFileName(fileData['uploadDetails']['fileName']);

        this.setItemState(item, status.Uploaded);
        this.getFileNameElement(item).textContent = fileName;

        file.dataset.multiFileUploadFileRef = fileData['reference'];

        return item;
    }

    /**
     * Checks if the system is busy i.e. is there a file still uploading or is there a file still being verified.
     */
    isBusy() {
        const stillUploading = this.container.querySelector(`.${this.classes.uploading}`) !== null;
        const stillVerifying = this.container.querySelector(`.${this.classes.verifying}`) !== null;

        return stillUploading || stillVerifying;
    }

    /**
     * Creates a new file upload item and focuses on that element.
     */
    handleAddItem() {
        const item = this.addItem();
        const file = this.getFileFromItem(item);

        file.focus();
    }

    /**
     * Adds an item to the list of upload items, specifying the current number and index of the file, and makes sure
     * that the events are bound to it when the user wants to upload a file.
     */
    addItem() {
        const item = this.parseHtml(this.itemTpl, {
            fileNumber: (this.getItems().length + 1).toString(),
            fileIndex: (++this.lastFileIndex).toString()
        });

        this.bindUploadEvents(item);
        this.itemList.append(item);

        this.updateButtonVisibility();

        return item;
    }

    /**
     * Gets the URL to check on file status.
     */
     getStatusUrl(fileRef) {
        return this.parseTemplate(this.config.statusUrlTpl, { fileRef: fileRef });
    }

    /**
     * Parses a string to HTML.
     */
    parseHtml(string, model) {
        string = this.parseTemplate(string, model);

        const dp = new DOMParser();
        const doc = dp.parseFromString(string, 'text/html');

        return doc.body.firstChild;
    }

    /**
     * Parses a template string to HTML.
     */
    parseTemplate(string, model) {
        for (const [key, value] of Object.entries(model)) {
            string = string.replace(new RegExp('{' + key + '}', 'g'), value.toString());
        }
        return string;
    }

    /**
     * Get all the upload items.
     */
    getItems() {
        return Array.from(this.itemList.querySelectorAll(`.${this.classes.item}`));
    }

    /**
     * Gets the file from the specified item.
     */
    getFileFromItem(item)  {
        return item.querySelector(`.${this.classes.file}`);
    }

    /**
     * Shows the 'Add another document' button if there is more files that can be uploaded.
     */
    updateButtonVisibility() {
        const itemCount = this.getItems().length;

        this.toggleAddButton(itemCount < this.config.maxFiles);
    }

    /**
     * Toggles the 'Add another document' button based on the state of the uploads.
     */
    toggleAddButton(state) {
        this.toggleElement(this.addAnotherBtn, state);
    }

    /**
     * Toggle the element by the reverse of its state.
     */
    toggleElement(element, state) {
        element.classList.toggle('hidden', !state);
    }

    /**
     * Removes all upload items.
     */
    removeAllItems() {
        this.getItems().forEach(item => item.remove());
    }

    /**
     * Gets the item from a file.
     */
    getItemFromFile(file) {
        return file.closest(`.${this.classes.item}`);
    }
}

/**
 * Adds event listener on page load, gets the form by its ID and then creates an MultiFileUpload with the form stored within.
 * Initialises the base state of the MultiFileUpload object.
 */
document.addEventListener('DOMContentLoaded', function() {
    const element = document.getElementById("multi-upload-form");
    const uploadObj = new MultiFileUpload(element);
    uploadObj.init();
});