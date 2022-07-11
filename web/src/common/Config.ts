import GitInfo from 'react-git-info/macro';

const longGitInfo = GitInfo();

export interface ShortGitInfo {
    readonly branch?: string;
    readonly date: string;
    readonly shortHash: string;
    readonly tags: ReadonlyArray<string>;
}

export const shortGitInfo: ShortGitInfo = {
    branch: longGitInfo.branch,
    date: longGitInfo.commit.date,
    shortHash: longGitInfo.commit.shortHash,
    tags: longGitInfo.tags,
};

export function getCompilerUrl(href: string): string {
    const url = new URL(href);
    url.hash = '';
    // url.pathname always starts with '/'.
    url.pathname = url.pathname.replace(/\/?(index\.html)?$/, '/compiler');
    url.search = '';
    return url.toString();
}

export const compilerUrl = process.env.REACT_APP_COMPILER_URL || getCompilerUrl(window.location.href);
