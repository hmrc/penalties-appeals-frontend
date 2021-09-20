"use strict";
class MultiFileUpload {
    constructor(form) {
        this.container = form;
        this.lastFileIndex = 0;
        this.config = {
            startRows: parseInt(form.dataset.multiFileUploadStartRows) || 1,
            minFiles: parseInt(form.dataset.multiFileUploadMinFiles) || 1,
            maxFiles: parseInt(form.dataset.multiFilUeploadMaxFiles) || 5,
            uploadedFiles: form.dataset.multiFileUploadUploadedFiles ? JSON.parse(form.dataset.multiFileUploadUploadedFiles) : []
        }

        this.classes = {
            itemList: 'multi-file-upload__item-list',
            item: 'multi-file-upload__item',
            file: 'multi-file-upload__file',
            fileNumber: 'multi-file-upload__number',
            addAnother: 'multi-file-upload__add-another'
        }

        this.cacheElements();
        this.cacheTemplates();
        this.bindEvents();
    }

    cacheElements() {
        this.itemList = this.container.querySelector(`.${this.classes.itemList}`);
        this.addAnotherBtn = this.container.querySelector(`.${this.classes.addAnother}`);
        this.formStatus = this.container.querySelector(`.${this.classes.formStatus}`);
        this.submitBtn = this.container.querySelector(`.${this.classes.submitBtn}`);
    }

    cacheTemplates() {
        this.itemTpl = document.getElementById('multi-file-upload-item-tpl').textContent;
    }

    bindEvents() {
        this.addAnotherBtn.addEventListener('click', this.handleAddItem.bind(this));
    }

    init() {
        this.updateButtonVisibility();
        this.removeAllItems();
        this.createInitialRows();
    }

     createInitialRows() {
        let rowCount = 0;

         this.config.uploadedFiles.forEach(fileData => {
             this.createUploadedItem(fileData);
             rowCount++;
         });

        if (rowCount < this.config.startRows) {
            for (let a = rowCount; a < this.config.startRows; a++) {
                this.addItem();
            }
        }
    }

    handleAddItem() {
        const item = this.addItem();
        const file = this.getFileFromItem(item);

        file.focus();
    }

    addItem() {
        const item = this.parseHtml(this.itemTpl, {
            fileNumber: (this.getItems().length + 1).toString(),
            fileIndex: (++this.lastFileIndex).toString()
        });

        this.itemList.append(item);

        this.updateButtonVisibility();

        return item;
    }

    parseHtml(string, model) {
        string = this.parseTemplate(string, model);

        const dp = new DOMParser();
        const doc = dp.parseFromString(string, 'text/html');

        return doc.body.firstChild;
    }

    parseTemplate(string, model) {
        for (const [key, value] of Object.entries(model)) {
            string = string.replace(new RegExp('{' + key + '}', 'g'), value.toString());
        }

        return string;
    }

    getItems() {
        return Array.from(this.itemList.querySelectorAll(`.${this.classes.item}`));
    }

    getFileFromItem(item)  {
        return item.querySelector(`.${this.classes.file}`);
    }

    updateButtonVisibility() {
        const itemCount = this.getItems().length;

        this.toggleAddButton(itemCount < this.config.maxFiles);
    }

    toggleAddButton(state) {
        this.toggleElement(this.addAnotherBtn, state);
    }

    toggleElement(element, state) {
        element.classList.toggle('hidden', !state);
    }

    removeAllItems() {
        this.getItems().forEach(item => item.remove());
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const element = document.getElementById("multi-upload-form");
    const uploadObj = new MultiFileUpload(element);
    uploadObj.init();
});