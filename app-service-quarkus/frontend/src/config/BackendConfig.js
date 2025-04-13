const devConfig = {
    API_URL: 'http://localhost:3001/todos'
};

const prodConfig = {
    API_URL: '/resources/todo'
};

const config = process.env.NODE_ENV === 'development' ? devConfig : prodConfig;
export default config;