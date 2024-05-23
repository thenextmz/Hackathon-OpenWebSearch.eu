import { FullText } from '@/types/fullText';
import { QueryParams, ApiResponse } from '@/types/queryParams';

class ApiClient {
  private baseURL: string;

  constructor() {
    this.baseURL = 'http://localhost:8008/';
  }

  /**
   * TODO: Implement and Test, change "any"
   * @param id
   * @param column
   * @returns
   */
  async fullText(id: string, column?: number): Promise<FullText> {
    const queryParams = new URLSearchParams();
    if (id) queryParams.append('id', id);
    if (column) queryParams.append('column', column.toString());

    const url = this.baseURL + `full-text?${queryParams.toString()}`;
    const res = await fetch(url);

    if (!res.ok) {
      throw new Error('Failed to fetch data');
    }

    return res.json();
  }

  /**
   * Search XML
   * TODO: Implement, change "any"
   * @param params
   */
  async searchXML(params: QueryParams): Promise<any> {}

  /**
   * Search
   * @param params QueryParams
   * @returns Query Results in JSON format
   */
  async search(params: QueryParams): Promise<ApiResponse> {
    const queryParams = new URLSearchParams();

    if (params.query) queryParams.append('q', params.query);
    if (params.index) queryParams.append('index', params.index);
    if (params.language) queryParams.append('lang', params.language);
    if (params.limit) queryParams.append('limit', params.limit.toString());
    if (params.keyword) queryParams.append('keyword', params.keyword);
    if (params.west != undefined && params.east != undefined && params.south != undefined && params.north != undefined) {
      queryParams.append('west', params.west.toString());
      queryParams.append('east', params.east.toString());
      queryParams.append('north', params.north.toString());
      queryParams.append('south', params.south.toString());
    }

    const url = this.baseURL + `search?${queryParams.toString()}`;
    console.log(url);
    const res = await fetch(url);

    if (!res.ok) {
      throw new Error('Failed to fetch data');
    }

    return res.json();
  }
}

export default ApiClient;
