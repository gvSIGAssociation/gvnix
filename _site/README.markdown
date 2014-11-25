Github Pages with Bootstrap 3 fork of jekyll-bootstrap with Omega Theme.
Improvement multilanguage.


# Introduction

gvNIX's project pages are based on [Jekyll](http://jekyllrb.com) and [GitHub Pages](http://pages.github.com/).
This means that project's website is stored within the project repo, in a special branch called "gh-pages".

If you're just getting started, then follow the directions immediately below.

# How to start a new `gh-pages` project page

From within your gvNIX project's git checkout directory:

### Create and checkout the gh-pages branch

    git checkout --orphan gh-pages

### Remove all files

    git rm -rf `git ls-files` && rm -rf *

### Add the gh-pages-upstream remote

    git remote add gh-pages-upstream https://github.com/disid/gvnix/gh-pages.git

### Pull in the common site infrastructure

    git pull gh-pages-upstream gh-pages


## Edit `_config.yml`

You'll need to tweak a few settings in `_config.yml`, sitting in the root directory.
Edit this file and follow the instructions within.


## Edit the content of your home page

### Edit the YAML front matter

You'll find "YAML front matter" at the top of the file, i.e. everything between the triple dashes that look like `---`.
This section contains some basic metadata.

TBC details for our theme


## View the site locally

Assuming you're already within your project's clone directory,
and you've already checked out the `gh-pages` branch,
follow these simple directions to view your site locally:

### Install jekyll if you have not already

    gem install bundler
    bundle

### Run jekyll

Use the `--watch` flag to pick up changes to files as you make them, allowing you a nice edit-and-refresh workflow.

    jekyll serve --watch

> **Important:** Because the `baseurl` is set explicitly within your project's
`_config.yml` file, you'll need to fully-qualify the URL to view your project.
For example, if your project is named "gvnix-xyz", your URL when running Jekyll
locally will be <http://localhost:4000/gvnix-xyz/>.
Don't forget the trailing slash! You'll get a 404 without it.


## Commit your changes

Once you're satisified with your edits, commit your changes and push the
 `gh-pages` pages up to your project's `origin` remote.

    git commit -m "Initialize project page"
    git push --set-upstream origin gh-pages
    git push


## View your site live on the web

That's it! After not more than a few minutes,
you should be able to see your site at http://project.github.io/{your-project}


