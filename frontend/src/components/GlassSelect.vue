<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';

const props = defineProps({
  modelValue: { type: [String, Number], default: '' },
  options: { type: Array, required: true },
  placeholder: { type: String, default: 'Seleziona...' }
});

const emit = defineEmits(['update:modelValue']);

const isOpen = ref(false);
const containerRef = ref(null);

const selectedLabel = computed(() => {
  const selected = props.options.find(o => o.value === props.modelValue);
  return selected ? selected.label : props.placeholder;
});

const toggleOpen = () => isOpen.value = !isOpen.value;

const selectOption = (val) => {
  emit('update:modelValue', val);
  isOpen.value = false;
};

const handleClickOutside = (e) => {
  if (containerRef.value && !containerRef.value.contains(e.target)) {
    isOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<template>
  <div class="custom-glass-select" ref="containerRef">
    <div class="select-trigger" @click="toggleOpen" :class="{ 'is-open': isOpen }">
      <span class="trigger-label">{{ selectedLabel }}</span>
      <svg class="trigger-icon" :class="{ 'rotated': isOpen }" stroke="currentColor" fill="none" stroke-width="2" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg">
        <polyline points="6 9 12 15 18 9"></polyline>
      </svg>
    </div>
    
    <transition name="dropdown-fade">
      <ul v-if="isOpen" class="select-options">
        <li 
          v-for="opt in options" 
          :key="opt.value" 
          class="option-item"
          :class="{ 'selected': opt.value === modelValue }"
          @click="selectOption(opt.value)"
        >
          {{ opt.label }}
          <svg v-if="opt.value === modelValue" class="check-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"></path>
          </svg>
        </li>
      </ul>
    </transition>
  </div>
</template>

<style scoped>
.custom-glass-select {
  position: relative;
  width: 220px;
  font-family: 'Inter', sans-serif;
  user-select: none;
}

.select-trigger {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  color: #1e293b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.select-trigger:hover, .select-trigger.is-open {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
}

.trigger-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.trigger-icon {
  width: 16px;
  height: 16px;
  color: #475569;
  transition: transform 0.3s ease;
}

.trigger-icon.rotated {
  transform: rotate(180deg);
}

.select-options {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 100%;
  margin: 0;
  padding: 6px;
  list-style: none;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.15), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  z-index: 50;
  max-height: 250px;
  overflow-y: auto;
}

.select-options::-webkit-scrollbar {
  width: 6px;
}
.select-options::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.3);
  border-radius: 10px;
}
.select-options::-webkit-scrollbar-track {
  background: transparent;
}

.option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  font-size: 14px;
  color: #334155;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.15s;
}

.option-item:hover {
  background: rgba(99, 102, 241, 0.1);
  color: #1e40af;
}

.option-item.selected {
  background: rgba(99, 102, 241, 0.15);
  color: #4338ca;
  font-weight: 600;
}

.check-icon {
  width: 16px;
  height: 16px;
  color: #4338ca;
}

/* Animazioni */
.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
  transform-origin: top;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: scaleY(0.95) translateY(-5px);
}
</style>
