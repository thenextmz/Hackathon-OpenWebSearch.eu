type SortOrder = 'desc' | 'asc';

export type QueryParams = {
  query: string;
  index?: string;
  language?: string;
  limit?: number;
  keyword?: string;
  ranking?: SortOrder;
  pw?: number;
  fulltext?: boolean;
  north?: number;
  south?: number;
  east?: number;
  west?: number;
};

export interface ApiResponse {
  results: {
    [key: string]: Result[][];
  };
}

export interface Result {
  id: string;
  url: string;
  title: string;
  textSnippet: string;
  language: string;
  warcDate: number;
  wordCount: number;
  locations: Location[];
  keywords: string[];
}

export interface Location {
  locationName: string;
  locationEntries: LocationEntry[];
}

export interface LocationEntry {
  latitude: number;
  longitude: number;
  alpha2CountryCode: string;
}
