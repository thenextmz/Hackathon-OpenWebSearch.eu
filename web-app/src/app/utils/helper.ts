import { Result } from '@/types/queryParams';

export function getIndexName(result: Result) {
  console.log('Hier:', result);
  return 'Test';
}

export function ISODateString(timestamp: number) {
  timestamp = timestamp / 1000;
  var d = new Date(timestamp);
  return d.getUTCFullYear() + '-' + pad(d.getUTCMonth() + 1) + '-' + pad(d.getUTCDate()) + ' ' + pad(d.getUTCHours()) + ':' + pad(d.getUTCMinutes());
}

function pad(n: number) {
  return n < 10 ? '0' + n : n;
}

export function getDescription(textSnippet: string) {
  return textSnippet.substring(0, 250);
}
