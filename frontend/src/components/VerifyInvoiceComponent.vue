<script setup lang="ts">
import {ref} from "vue";
import UploadIcon from "@/components/icons/UploadIcon.vue";
import StatusInfoComponent from "@/components/StatusInfoComponent.vue";
import {useInvoiceStore} from "@/stores/invoice";

const invoice = useInvoiceStore();

type fileStatus = "readyForUpload" | "uploading" | "success" | "failed";

const currentStatus = ref<fileStatus>("readyForUpload");
const error = ref<string>("");
const file = ref<File | null>(null);
const fileInput = ref<HTMLInputElement>();

function handleInputFile() {
  let tempFile: File | null | undefined =
      fileInput.value && fileInput.value.files && fileInput.value.files.item(0);
  processFile(tempFile);
}

function onDrop(e: DragEvent) {
  let tempFile: File | null = e.dataTransfer && e.dataTransfer.files.item(0);
  processFile(tempFile);
}

function processFile(tmpFile: File | null | undefined) {
  error.value = "";

  if (!tmpFile) {
    return;
  }
  if (tmpFile && tmpFile.type !== "text/xml") {
    error.value = "Please upload correct file format.";
    return;
  }
  file.value = tmpFile;
}

function preventDefaults(e: DragEvent) {
  e.preventDefault();
}

async function verifyFile() {
  if (!file.value) {
    return;
  }
  currentStatus.value = "uploading";
  const res: any = await invoice.verifyInvoice(file.value);
  currentStatus.value = res.verified ? "success" : "failed";
}

function verifyFileAgain() {
  file.value = null;
  currentStatus.value = "readyForUpload";
}
</script>
<template>
  <div class="verify-invoice-component">
    <h2>Verify Invoice</h2>
    <div class="upload-container">
      <div class="upload-box">
        <div
            v-if="currentStatus === 'readyForUpload'"
            id="drop-area"
            class="drag-n-drop-box"
            :class="{ error: error }"
            @dragenter="preventDefaults"
            @dragover="preventDefaults"
            @dragleave="preventDefaults"
            @drop.prevent="onDrop"
            @click="fileInput?.click()"
        >
          <input
              type="file"
              ref="fileInput"
              class="hidden"
              @change="handleInputFile()"
          />
          <UploadIcon/>
          <span>{{
              (file && file.name) ||
              "Drag & drop UBL file to upload or click to browse"
            }}</span>
        </div>
        <div v-if="currentStatus === 'uploading'" class="progress-box">
          <sl-progress-bar value="50"></sl-progress-bar>
          <span class="progress-status">VERIFYING DOCUMENT...</span>
        </div>
        <StatusInfoComponent
            v-if="currentStatus === 'success'"
            :successful="true"
        />
        <StatusInfoComponent
            v-if="currentStatus === 'failed'"
            :successful="false"
        />
      </div>
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </div>

    <sl-button
        v-if="currentStatus === 'readyForUpload'"
        variant="primary"
        @click="verifyFile()"
    >Verify
    </sl-button
    >
    <sl-button v-else-if="currentStatus === 'uploading'">
      <sl-spinner></sl-spinner
      >
    </sl-button>
    <sl-button v-else variant="primary" @click="verifyFileAgain()"
    >Verify another
    </sl-button
    >
  </div>
</template>

<style scoped lang="scss">
.verify-invoice-component {
  width: 639px;
  height: 302px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  background: var(--white);
  border: 1px solid var(--light-light-gray);
  border-radius: 4px;

  .upload-container {
    height: 150px;
    width: 100%;
  }

  .upload-box {
    height: 125px;
    width: 100%;

    div.drag-n-drop-box {
      display: flex;
      justify-content: center;
      border: 1px solid var(--light-light-gray);
      border-radius: 4px;
      min-height: 119px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      span {
        font-style: normal;
        font-weight: 400;
        font-size: 14px;
        line-height: 16px;
        color: var(--primary-color);
      }

      &.error {
        border: 1px solid var(--warning);
      }
    }

    .progress-box {
      border: 1px solid var(--light-light-gray);
      border-radius: 4px;
      min-height: 119px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;

      .progress-status {
        color: var(--primary-color);
        font-style: normal;
        font-weight: 700;
        font-size: 10px;
        line-height: 150%;
        width: 500px;
      }
    }
  }

  .error-message {
    font-weight: 400;
    font-size: 12px;
    line-height: 14px;
    color: var(--warning);
    align-self: flex-start;
  }

  sl-progress-bar {
    width: 500px;
    --height: 8px;
    --indicator-color: var(--primary-color);
    --track-color: var(--light-gray);
  }
}

h2 {
  color: var(--primary-color);
  font-style: normal;
  font-weight: 700;
  font-size: 20px;
  line-height: 23px;
  text-align: center;
  margin-bottom: 20px;
}

sl-button::part(base) {
  width: 209px;
  height: 48px;
  background-color: var(--primary-color-lightest);
  color: var(--white);
  border-radius: 4px;
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 48px;
}

sl-spinner::part(base) {
  --indicator-color: var(--white);
}
</style>
