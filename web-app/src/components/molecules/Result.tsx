import { ISODateString, getDescription, getIndexName } from '@/app/utils/helper';
import { ApiResponse, QueryParams } from '@/types/queryParams';
import Link from 'next/link';
import { FC } from 'react';

interface ResultSectionProps {
  results: ApiResponse;
  queryParams: QueryParams;
}

export const Result: FC<ResultSectionProps> = ({ results, queryParams }) => {
  const indices = Object.keys(results.results).map((key) => Object.keys(results.results[key])[0]);

  return (
    <div className="">
      {indices.map((indexname: any, index) => {
        const items = results.results[index][indexname];
        return (
          <div className="p-4 bg-theme rounded-md mb-8" key={index}>
            <div className="flex items-center justify-between pb-4">
              <p className="flex flex-1 justify-start uppercase text-white text-2xl text-semibold">{indexname}</p>
              <p className="flex flex-1 justify-center uppercase text-white text-2xl text-semibold">{' · '}</p>
              <p className="flex flex-1 justify-end uppercase text-white text-2xl text-semibold">{items.length} Elements</p>
            </div>

            {items.map((item, itemIndex) => {
              const language = item.language;
              const wordCount = item.wordCount;
              const date = ISODateString(item.warcDate);

              return (
                <div className="bg-blue-200 flex flex-col" key={itemIndex}>
                  <p>Title: {item.title}</p>
                  <p>Description: {getDescription(item.textSnippet)}</p>
                  <p>
                    Metadata: language: {language}, word count: {wordCount}, index date: {date}
                  </p>

                  <div className="bg-yellow-200 flex flex-wrap ">
                    {item.locations.map((location, locationIndex) => {
                      const name = location.locationName;
                      const long = location.locationEntries[0].longitude;
                      const lat = location.locationEntries[0].latitude;
                      const lurl = 'https://www.openstreetmap.org/?mlat=' + lat + '&mlon=' + long + '#map=6/' + lat + '/' + long;

                      return (
                        <>
                          {locationIndex == 0 && <p>Location: </p>}
                          <Link key={locationIndex} style={{ color: 'blue' }} href={lurl}>
                            {name}
                            {locationIndex !== item.locations.length - 1 && ' · '}
                          </Link>
                        </>
                      );
                    })}
                  </div>

                  {item.keywords.map((keyword, keywordIndex) => {
                    return (
                      <p key={keywordIndex}>
                        {keyword}
                        {keywordIndex !== item.keywords.length - 1 && ' · '}
                      </p>
                    );
                  })}

                  <Link style={{ color: 'blue' }} href={item.url}>
                    <p>{item.url}</p>
                  </Link>
                </div>
              );
            })}
          </div>
        );
      })}
    </div>
  );
};
