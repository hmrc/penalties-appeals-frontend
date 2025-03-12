const path = require('path');
const ESLintPlugin = require('eslint-webpack-plugin');

module.exports = function (env = {}) {
    const entry = env.entry ? Object.values(env.entry) : [];  // Ensure env.entry is not undefined or null

    return {
        mode: 'production',
        optimization: {
            minimize: true,
            concatenateModules: true
        },
        watch: false,
        devtool: 'source-map',
        entry: entry,
        resolve: {
            extensions: ['.js'],
            alias: {
                'node_modules': path.join(__dirname, 'node_modules'),
                'webjars': env.webjars ? env.webjars.path : '',  // Ensure env.webjars is available
            }
        },
        module: {
            rules: [
                {
                    test: /\.js$/,
                    exclude: /node_modules/,
                    use: {
                        loader: 'babel-loader',
                        options: {
                            presets: ['@babel/preset-env'],
                            plugins: ['@babel/plugin-proposal-class-properties']
                        }
                    }
                }
            ]
        },
        plugins: [
            new ESLintPlugin({
                extensions: ['js'],
                exclude: ['node_modules', 'legacy']
            })
        ],
        output: env.output || {}  // Provide a fallback for output
    };
};
