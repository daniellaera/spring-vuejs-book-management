<template>
  <section class="home-view">
    <div class="book-list-section">
      <BookList />
    </div>
    <footer class="home-footer">
      Version: {{ version }} | Build: {{ buildTime }}
    </footer>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import BookList from "@/views/BookList.vue";
import { getVersion, type VersionInfo } from "@/service/versionService";

const version = ref("loading...");
const buildTime = ref("");

onMounted(async () => {
  const versionInfo: VersionInfo = await getVersion();
  version.value = versionInfo.version;
  buildTime.value = versionInfo.buildTime;
});
</script>

<style scoped>
.home-view {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  padding-top: 80px;
}

.book-list-section {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.home-footer {
  margin-top: 2rem;
  font-size: 0.9rem;
  color: #666;
}

@media (max-width: 1024px) {
  .home-view {
    padding-top: 60px;
  }
}
</style>
