export const UI_THEME_STORAGE_KEY = "aimaid_global_ui_theme";

export const DEFAULT_UI_THEME = {
  "--bg-page": "#19131a", "--bg-sidebar": "#120e13", "--bg-surface": "#221923", "--bg-card": "#2b202c", "--bg-card-hover": "#352637", "--bg-selected": "#4a2943",
  "--border-subtle": "#3a2c3b", "--border-normal": "#513b50", "--border-focus": "#ff8fbe",
  "--text-primary": "#fff3f8", "--text-secondary": "#d8bcc9", "--text-muted": "#967d89",
  "--accent-primary": "#ff8fbe", "--accent-secondary": "#c69cff", "--accent-soft": "#ffb8d4", "--accent-blue": "#82b7ff", "--accent-cyan": "#6fe2df", "--accent-mint": "#72ddb1", "--accent-yellow": "#ffc978", "--accent-red": "#ff718f",
  "--card-radius": "16px", "--control-radius": "10px", "--card-shadow-size": "30px",
};

const shape = { "--card-radius": "16px", "--control-radius": "10px", "--card-shadow-size": "30px" };
const preset = (id, name, description, theme) => ({ id, name, description, theme: { ...theme, ...shape } });

export const UI_THEME_PRESETS = [
  { id: "powder-pink", name: "粉紫糖果", description: "温暖柔和的粉紫渐变", theme: DEFAULT_UI_THEME },
  preset("catppuccin-mocha", "Catppuccin Mocha", "柔和、低刺激的粉彩暗色", {
    "--bg-page": "#1e1e2e", "--bg-sidebar": "#11111b", "--bg-surface": "#181825", "--bg-card": "#313244", "--bg-card-hover": "#45475a", "--bg-selected": "#45475a", "--border-subtle": "#45475a", "--border-normal": "#6c7086", "--border-focus": "#cba6f7", "--text-primary": "#cdd6f4", "--text-secondary": "#bac2de", "--text-muted": "#7f849c", "--accent-primary": "#cba6f7", "--accent-secondary": "#f5c2e7", "--accent-soft": "#b4befe", "--accent-blue": "#89b4fa", "--accent-cyan": "#89dceb", "--accent-mint": "#a6e3a1", "--accent-yellow": "#f9e2af", "--accent-red": "#f38ba8",
  }),
  preset("dracula", "Dracula", "经典、高对比的紫粉暗色", {
    "--bg-page": "#282a36", "--bg-sidebar": "#191a21", "--bg-surface": "#21222c", "--bg-card": "#343746", "--bg-card-hover": "#424450", "--bg-selected": "#44475a", "--border-subtle": "#44475a", "--border-normal": "#6272a4", "--border-focus": "#bd93f9", "--text-primary": "#f8f8f2", "--text-secondary": "#d6d6cf", "--text-muted": "#6272a4", "--accent-primary": "#bd93f9", "--accent-secondary": "#ff79c6", "--accent-soft": "#d6acff", "--accent-blue": "#bd93f9", "--accent-cyan": "#8be9fd", "--accent-mint": "#50fa7b", "--accent-yellow": "#f1fa8c", "--accent-red": "#ff5555",
  }),
  preset("nord", "Nord", "克制、清爽的极地冰蓝", {
    "--bg-page": "#2e3440", "--bg-sidebar": "#242933", "--bg-surface": "#3b4252", "--bg-card": "#434c5e", "--bg-card-hover": "#4c566a", "--bg-selected": "#3f5368", "--border-subtle": "#4c566a", "--border-normal": "#5e6b7d", "--border-focus": "#88c0d0", "--text-primary": "#eceff4", "--text-secondary": "#d8dee9", "--text-muted": "#8b96a8", "--accent-primary": "#88c0d0", "--accent-secondary": "#b48ead", "--accent-soft": "#8fbcbb", "--accent-blue": "#81a1c1", "--accent-cyan": "#88c0d0", "--accent-mint": "#a3be8c", "--accent-yellow": "#ebcb8b", "--accent-red": "#bf616a",
  }),
  preset("tokyo-night", "Tokyo Night", "深蓝底色与都市霓虹点缀", {
    "--bg-page": "#1a1b26", "--bg-sidebar": "#16161e", "--bg-surface": "#1f2335", "--bg-card": "#24283b", "--bg-card-hover": "#292e42", "--bg-selected": "#33467c", "--border-subtle": "#3b4261", "--border-normal": "#545c7e", "--border-focus": "#7aa2f7", "--text-primary": "#c0caf5", "--text-secondary": "#a9b1d6", "--text-muted": "#565f89", "--accent-primary": "#7aa2f7", "--accent-secondary": "#bb9af7", "--accent-soft": "#7dcfff", "--accent-blue": "#7aa2f7", "--accent-cyan": "#7dcfff", "--accent-mint": "#9ece6a", "--accent-yellow": "#e0af68", "--accent-red": "#f7768e",
  }),
  preset("gruvbox", "Gruvbox", "复古温暖、耐看的大地色", {
    "--bg-page": "#282828", "--bg-sidebar": "#1d2021", "--bg-surface": "#32302f", "--bg-card": "#3c3836", "--bg-card-hover": "#504945", "--bg-selected": "#665c54", "--border-subtle": "#504945", "--border-normal": "#665c54", "--border-focus": "#fe8019", "--text-primary": "#ebdbb2", "--text-secondary": "#d5c4a1", "--text-muted": "#928374", "--accent-primary": "#fe8019", "--accent-secondary": "#d3869b", "--accent-soft": "#fabd2f", "--accent-blue": "#83a598", "--accent-cyan": "#8ec07c", "--accent-mint": "#b8bb26", "--accent-yellow": "#fabd2f", "--accent-red": "#fb4934",
  }),
  preset("one-dark", "One Dark", "现代中性、清晰的编辑器风格", {
    "--bg-page": "#282c34", "--bg-sidebar": "#21252b", "--bg-surface": "#2c313a", "--bg-card": "#333842", "--bg-card-hover": "#3e4451", "--bg-selected": "#3e4451", "--border-subtle": "#3e4451", "--border-normal": "#5c6370", "--border-focus": "#61afef", "--text-primary": "#abb2bf", "--text-secondary": "#9da5b4", "--text-muted": "#5c6370", "--accent-primary": "#61afef", "--accent-secondary": "#c678dd", "--accent-soft": "#56b6c2", "--accent-blue": "#61afef", "--accent-cyan": "#56b6c2", "--accent-mint": "#98c379", "--accent-yellow": "#e5c07b", "--accent-red": "#e06c75",
  }),
];

export function isUiThemePreset(theme, candidate) {
  return Object.entries(candidate.theme).every(([key, value]) => theme[key] === value);
}

export function readUiTheme() {
  try { return { ...DEFAULT_UI_THEME, ...JSON.parse(localStorage.getItem(UI_THEME_STORAGE_KEY) || "{}") }; }
  catch { return { ...DEFAULT_UI_THEME }; }
}

export function applyUiTheme(theme) {
  Object.entries({ ...DEFAULT_UI_THEME, ...theme }).forEach(([name, value]) => document.documentElement.style.setProperty(name, value));
}

export function saveUiTheme(theme) {
  localStorage.setItem(UI_THEME_STORAGE_KEY, JSON.stringify(theme));
  applyUiTheme(theme);
}

export function applyStoredUiTheme() { applyUiTheme(readUiTheme()); }
