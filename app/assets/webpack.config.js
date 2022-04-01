const path = require('path');

module.exports = function (env) {
    return {
        mode: 'production',
        optimization: {
            minimize: true,
            concatenateModules: true
        },
        watch: false,
        devtool: 'source-map',
        entry: Object.values(env.entry),
        resolve: {
            extensions: ['.js'],
            alias: {
                'node_modules': path.join(__dirname, 'node_modules'),
                'webjars': env.webjars.path
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
                            presets: [
                                '@babel/preset-env'
                            ],
                            plugins: [
                                '@babel/plugin-proposal-class-properties'
                            ]
                        }
                    }
                },
                {
                    test: /\.js$/,
                    exclude: /node_modules|legacy/,
                    loader: 'eslint-loader'
                }
            ]
        },
        output: env.output
    };
};