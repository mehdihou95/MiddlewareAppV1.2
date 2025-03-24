import axios from 'axios';

const CLIENT_ID_HEADER = 'X-Client-ID';
const CLIENT_NAME_HEADER = 'X-Client-Name';

export const setClientContext = (clientId?: number, clientName?: string) => {
  if (clientId) {
    axios.defaults.headers.common[CLIENT_ID_HEADER] = clientId.toString();
  } else {
    delete axios.defaults.headers.common[CLIENT_ID_HEADER];
  }

  if (clientName) {
    axios.defaults.headers.common[CLIENT_NAME_HEADER] = clientName;
  } else {
    delete axios.defaults.headers.common[CLIENT_NAME_HEADER];
  }
};

export const clearClientContext = () => {
  delete axios.defaults.headers.common[CLIENT_ID_HEADER];
  delete axios.defaults.headers.common[CLIENT_NAME_HEADER];
}; 