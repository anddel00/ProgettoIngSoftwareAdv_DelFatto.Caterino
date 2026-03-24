<script setup>
import { ref } from 'vue'

// Dati finti (Mock) che in futuro arriveranno da Spring Boot
const prodotti = ref([
  { id: 1, nome: "Vaccino Pfizer", spazioUnitario: 2, prezzo: 15.50, minTemp: -80, maxTemp: -20 },
  { id: 2, nome: "Mele Golden", spazioUnitario: 5, prezzo: 2.00, minTemp: 2, maxTemp: 8 },
  { id: 3, nome: "Detersivo Piatti", spazioUnitario: 10, prezzo: 1.50, minTemp: 0, maxTemp: 40 }
])
</script>

<template>
  <div class="dashboard-container">
    <h1>📦 Gestione Inventario WMS</h1>
    <p>Benvenuto nell'area Admin. Qui puoi monitorare le merci stoccate.</p>

    <table class="inventario-table">
      <thead>
      <tr>
        <th>ID</th>
        <th>Nome Prodotto</th>
        <th>Spazio (Unitario)</th>
        <th>Prezzo (€)</th>
        <th>Temp. Min</th>
        <th>Temp. Max</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="prodotto in prodotti" :key="prodotto.id">
        <td>{{ prodotto.id }}</td>
        <td><strong>{{ prodotto.nome }}</strong></td>
        <td>{{ prodotto.spazioUnitario }} u</td>
        <td>{{ prodotto.prezzo.toFixed(2) }}</td>
        <td class="freddo">{{ prodotto.minTemp }} °C</td>
        <td class="caldo">{{ prodotto.maxTemp }} °C</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.dashboard-container {
  font-family: Arial, sans-serif;
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

h1 {
  color: #2c3e50;
}

.inventario-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.inventario-table th, .inventario-table td {
  padding: 12px 15px;
  border: 1px solid #ddd;
  text-align: left;
}

.inventario-table th {
  background-color: #34495e;
  color: white;
}

.inventario-table tr:nth-child(even) {
  background-color: #f9f9f9;
}

.freddo { color: #2980b9; font-weight: bold; }
.caldo { color: #e74c3c; font-weight: bold; }
</style>