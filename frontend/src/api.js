import axios from 'axios'

// Creiamo un'istanza personalizzata di Axios
const api = axios.create({
    // import.meta.env è il modo in cui Vite legge il file .env
    baseURL: import.meta.env.VITE_API_URL,
    headers: {
        'Content-Type': 'application/json'
    }
})
// Interceptor per le richieste
api.interceptors.request.use(
    (config) => {
        const token = sessionStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

export default api