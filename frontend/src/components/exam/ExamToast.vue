<template>
  <div class="toast" :class="{ hidden: !state.toastVisible }">
    {{ state.toastMessage }}
  </div>
</template>

<script setup>
import { inject } from 'vue';
const global = inject('global');
const { state } = global;
</script>