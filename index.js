import 'react-native/Libraries/Core/InitializeCore';
import { AppRegistry } from 'react-native';
import React from 'react';
import { startup, components } from 'browser-core';
import 'babel-polyfill';

// set app global for debugging
const appStart = startup.then((app) => {
  global.app = app;
});

// register components from config
Object.keys(components).forEach((component) => {
  AppRegistry.registerComponent(component, () => class extends React.Component {
    render() {
      return React.createElement(components[component], { appStart });
    }
  });
});
