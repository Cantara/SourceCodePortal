var gulp        = require('gulp');
var browserSync = require('browser-sync').create();
var sass        = require('gulp-sass');
var uglifycss = require('gulp-uglifycss');

var config = {
    sass_source: 'scss/*.scss',
    css_target: 'css/',
    js_target: 'js/',
    css_uglified_source: 'css/*.css',
    css_uglified_target: './dist/',
    html_watchdir: '../resources/META-INF/views/*.html'
};

// Compile sass into CSS & auto-inject into browsers
gulp.task('sass', function() {
    return gulp.src(['node_modules/bootstrap/scss/bootstrap.scss', config.sass_source])
        .pipe(sass())
        .pipe(gulp.dest(config.css_target))
        .pipe(browserSync.stream());
});

gulp.task('css', function () {
    return gulp.src(config.css_uglified_source)
        .pipe(uglifycss({
            "uglyComments": true
        }))
        .pipe(gulp.dest(config.css_uglified_target))
        .pipe(browserSync.stream());
});


// Move the javascript files into our /src/js folder
gulp.task('js', function() {
    return gulp.src(['node_modules/bootstrap/dist/js/bootstrap.min.js', 'node_modules/jquery/dist/jquery.min.js', 'node_modules/popper.js/dist/umd/popper.min.js'])
        .pipe(gulp.dest(config.js_target))
        .pipe(browserSync.stream());
});

// Static Server + watching scss/html files
gulp.task('serve', gulp.series('sass'), function() {

    browserSync.init({
        server: "./"
    });

    // gulp.watch(['node_modules/bootstrap/scss/bootstrap.scss', config.sass_source], ['sass']);
    gulp.watch(config.html_watchdir).on('change', browserSync.reload);
});

gulp.task('default', gulp.parallel('sass', 'serve'));
