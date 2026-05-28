import {
  DataProvider,
  fetchUtils,
  RaRecord,
  Identifier,
  CreateParams,
  DeleteParams,
} from "react-admin";
import { stringify } from "query-string";

const apiUrl = `${process.env.NEXT_PUBLIC_API_URL}/data`;
const httpClient = (url: string, options: fetchUtils.Options = {}) => {
  options.credentials = "include";
  return fetchUtils.fetchJson(url, options);
};

export const dataProvider: DataProvider = {
  getList: async (resource, params) => {
    const { page, perPage } = params.pagination!;
    const { field, order } = params.sort!;
    const filter = params.filter;

    const query = {
      page: page - 1,
      perPage: perPage,
      sort: field,
      order: order,
      filter: JSON.stringify(filter),
    };
    const url = `${apiUrl}/${resource}?${stringify(query)}`;

    const { json } = await httpClient(url);

    return {
      data: json.data,
      total: json.total,
    };
  },

  getOne: async (resource, params) => {
    const { json } = await httpClient(`${apiUrl}/${resource}/${params.id}`);
    return {
      data: json.data,
    };
  },

  getMany: async (resource, params) => {
    const query = {
      ids: params.ids.join(","),
    };
    const url = `${apiUrl}/${resource}?${stringify(query)}`;
    const { json } = await httpClient(url);
    return {
      data: json.data,
    };
  },

  getManyReference: async (resource, params) => {
    const { page, perPage } = params.pagination;
    const { field, order } = params.sort;
    const query = {
      page: page - 1,
      perPage: perPage,
      sort: field,
      order: order,
      filter: JSON.stringify({
        ...params.filter,
        [params.target]: params.id,
      }),
    };
    const url = `${apiUrl}/${resource}?${stringify(query)}`;

    const { json } = await httpClient(url);
    return {
      data: json.data,
      total: json.total,
    };
  },

  update: async (resource, params) => {
    const { json } = await httpClient(`${apiUrl}/${resource}/${params.id}`, {
      method: "PUT",
      body: JSON.stringify(params.data),
    });
    return {
      data: json.data,
    };
  },

  updateMany: async (resource, params) => {
    const query = {
      ids: params.ids.join(","),
    };
    const { json } = await httpClient(
      `${apiUrl}/${resource}?${stringify(query)}`,
      {
        method: "PUT",
        body: JSON.stringify(params.data),
      }
    );
    return {
      data: json.data,
    };
  },

  create: async <RecordType extends Omit<RaRecord, "id">>(
    resource: string,
    params: CreateParams<RecordType>
  ) => {
    const { json } = await httpClient(`${apiUrl}/${resource}`, {
      method: "POST",
      body: JSON.stringify(params.data),
    });
    const createdRecord = { ...params.data, id: json.data.id };
    return {
      data: createdRecord as RecordType & { id: Identifier },
    };
  },

  delete: async <RecordType extends RaRecord>(
    resource: string,
    params: DeleteParams<RecordType>
  ) => {
    await httpClient(`${apiUrl}/${resource}/${params.id}`, {
      method: "DELETE",
    });
    return {
      data: params.previousData as RecordType,
    };
  },

  deleteMany: async (resource, params) => {
    const query = {
      ids: params.ids.join(","),
    };
    await httpClient(`${apiUrl}/${resource}?${stringify(query)}`, {
      method: "DELETE",
    });
    return {
      data: [],
    };
  },
};
