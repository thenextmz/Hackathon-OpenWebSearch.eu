import { IndexEntry } from '@/types/indexEntry';

export const indexIndices: { [key: string]: IndexEntry } = {
  default: { displayString: 'Default/All', apiString: '' },
  demoSimpleWiki: { displayString: 'Demo SimpleWiki', apiString: 'demo-simplewiki' },
  demoUnisGraz: { displayString: 'Demo Graz Universities', apiString: 'demo-unis-graz' },
  dlrPrototype: { displayString: 'DLR Prototype', apiString: 'dlrprototype' },
};

export const languageIndices: { [key: string]: IndexEntry } = {
  default: { displayString: 'Default/All', apiString: '' },
  english: { displayString: 'English', apiString: 'eng' },
  german: { displayString: 'German', apiString: 'deu' },
};
