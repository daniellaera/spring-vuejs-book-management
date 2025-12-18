import apiClient from "@/plugins/axiosConfig";

export interface VersionInfo {
  version: string;
  buildTime: string;
}

export async function getVersion(): Promise<VersionInfo> {
  try {
    const response = await apiClient.get<VersionInfo>('/version');
    return response.data;
  } catch (error) {
    console.error("Failed to fetch version:", error);
    return { version: "unknown", buildTime: "" };
  }
}
