import axios from 'axios'

// Creiamo un'istanza personalizzata di Axios
const api = axios.create({
    // import.meta.env è il modo in cui Vite legge il file .env
    baseURL: import.meta.env.VITE_API_URL,
    headers: {
        'Content-Type': 'application/json'
    }
})

export default api