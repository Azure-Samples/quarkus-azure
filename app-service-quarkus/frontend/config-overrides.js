// Fix https://github.com/fkhadra/react-toastify/issues/775
// https://github.com/reactioncommerce/reaction-component-library/issues/399#issuecomment-467860022
module.exports = function override(webpackConfig) {
    webpackConfig.module.rules.push({
      test: /\.mjs$/,
      include: /node_modules/,
      type: "javascript/auto"
    });
  
    return webpackConfig;
  }
  