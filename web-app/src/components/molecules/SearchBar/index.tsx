import { Button, Input, InputGroup, InputLeftElement, InputRightElement, Stack } from '@chakra-ui/react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSearch } from '@fortawesome/free-solid-svg-icons';
import { PhoneIcon, AddIcon, WarningIcon, SearchIcon } from '@chakra-ui/icons';
import { FC } from 'react';
import { QueryParams } from '@/types/queryParams';

interface SearchBarProps {
  queryParams: QueryParams;
  setQueryParams: (queryParams: QueryParams) => void;
  setQuery: (query: string) => void;
  loading: boolean;
  sendRequest: () => void;
}

export const SearchBar: FC<SearchBarProps> = ({ queryParams, setQueryParams, loading, sendRequest, setQuery }) => {
  return (
    <InputGroup>
      <Input
        placeholder="Search ..."
        color="white"
        rounded={'xl'}
        onChange={(e) => {
          setQueryParams({ ...queryParams, query: e.target.value });
        }}
        value={queryParams.query}
        onKeyUp={(e) => {
          if (e.key === 'Enter' && queryParams.query.length !== 0) {
            setQuery(queryParams.query);
            sendRequest();
          }
        }}
      />
      <InputRightElement color="gray.300" fontSize="1.2em">
        <Button
          roundedRight={'xl'}
          isLoading={loading}
          isDisabled={loading || queryParams.query.length === 0}
          onClick={() => {
            setQuery(queryParams.query);
            sendRequest();
          }}
        >
          <SearchIcon />
        </Button>
      </InputRightElement>
    </InputGroup>
  );
};
