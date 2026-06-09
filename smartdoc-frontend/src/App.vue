<template>
  <router-view v-slot="{ Component }">
    <transition mode="out-in" name="fade-page">
      <component :is="Component"/>
    </transition>
  </router-view>
</template>

<style>
.fade-page-enter-active,
.fade-page-leave-active {
  transition: opacity 0.344s cubic-bezier(0.25, 0.1, 0.25, 1), transform 0.344s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.fade-page-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.fade-page-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
